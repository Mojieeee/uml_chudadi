package com.example.uml_chudadi.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class MusicScene {
    Lobby,
    Game
}

private const val LICENSED_BGM_LOOP_START_MS = 0
private const val LICENSED_BGM_LOOP_END_MS = 93_000
private const val LICENSED_BGM_LOOP_POLL_MS = 180L

class CardRoomMusicPlayer(private val context: Context) {
    private val sampleRate = 22_050
    @Volatile private var running = false
    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var worker: Thread? = null

    fun start(scene: MusicScene) {
        stop()
        if (startLicensedAsset()) return
        running = true
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(max(minBuffer, sampleRate))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        audioTrack = track
        track.play()
        worker = thread(name = "card-room-music", isDaemon = true) {
            runCatching { playLoop(track, scene) }
        }
    }

    fun stop() {
        running = false
        worker?.interrupt()
        worker = null
        mediaPlayer?.let { player ->
            runCatching { player.stop() }
            runCatching { player.release() }
        }
        mediaPlayer = null
        audioTrack?.let { track ->
            runCatching { track.pause() }
            runCatching { track.flush() }
            runCatching { track.release() }
        }
        audioTrack = null
    }

    private fun startLicensedAsset(): Boolean {
        val assetName = listOf("doudizhu_bgm.ogg", "doudizhu_bgm.mp3")
            .firstOrNull { name -> runCatching { context.assets.openFd(name).close() }.isSuccess }
            ?: return false
        return runCatching {
            val descriptor = context.assets.openFd(assetName)
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            player.isLooping = false
            player.setVolume(0.55f, 0.55f)
            player.prepare()
            player.seekTo(LICENSED_BGM_LOOP_START_MS)
            player.setOnCompletionListener {
                runCatching {
                    it.seekTo(LICENSED_BGM_LOOP_START_MS)
                    it.start()
                }
            }
            player.start()
            mediaPlayer = player
            running = true
            worker = thread(name = "licensed-card-room-music-loop", isDaemon = true) {
                while (running) {
                    runCatching {
                        val activePlayer = mediaPlayer ?: return@thread
                        if (activePlayer.currentPosition >= LICENSED_BGM_LOOP_END_MS) {
                            activePlayer.seekTo(LICENSED_BGM_LOOP_START_MS)
                            if (!activePlayer.isPlaying) activePlayer.start()
                        }
                    }
                    runCatching { Thread.sleep(LICENSED_BGM_LOOP_POLL_MS) }
                        .onFailure { return@thread }
                }
            }
            true
        }.getOrDefault(false)
    }

    private fun playLoop(track: AudioTrack, scene: MusicScene) {
        val melody = when (scene) {
            MusicScene.Lobby -> doubleArrayOf(392.0, 493.88, 587.33, 659.25, 587.33, 493.88, 440.0, 392.0)
            MusicScene.Game -> doubleArrayOf(329.63, 392.0, 440.0, 493.88, 523.25, 493.88, 440.0, 392.0)
        }
        val beatMillis = when (scene) {
            MusicScene.Lobby -> 230
            MusicScene.Game -> 280
        }
        val volume = when (scene) {
            MusicScene.Lobby -> 0.11
            MusicScene.Game -> 0.08
        }
        var index = 0
        while (running) {
            val note = melody[index % melody.size]
            val accent = if (index % 4 == 0) 1.15 else 1.0
            writeNote(track, note, beatMillis, volume * accent)
            if (index % 8 == 7) writeNote(track, 0.0, 80, volume)
            index += 1
        }
    }

    private fun writeNote(track: AudioTrack, frequency: Double, durationMillis: Int, volume: Double) {
        val totalSamples = sampleRate * durationMillis / 1000
        val buffer = ShortArray(512)
        var sampleIndex = 0
        while (running && sampleIndex < totalSamples) {
            val count = min(buffer.size, totalSamples - sampleIndex)
            for (i in 0 until count) {
                val absolute = sampleIndex + i
                val envelope = min(1.0, min(absolute / 220.0, (totalSamples - absolute) / 360.0)).coerceAtLeast(0.0)
                val wave = if (frequency == 0.0) {
                    0.0
                } else {
                    val t = absolute.toDouble() / sampleRate
                    sin(2.0 * PI * frequency * t) + sin(2.0 * PI * frequency * 2.0 * t) * 0.18
                }
                buffer[i] = (wave * Short.MAX_VALUE * volume * envelope).toInt().toShort()
            }
            track.write(buffer, 0, count)
            sampleIndex += count
        }
    }
}
