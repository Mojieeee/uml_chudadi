package com.example.uml_chudadi.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MusicAssetTest {
    @Test
    fun backgroundMusicAssetIsTrimmedToNinetyThreeSeconds() {
        val asset = listOf(
            File("src/main/assets/doudizhu_bgm.ogg"),
            File("app/src/main/assets/doudizhu_bgm.ogg")
        ).first { it.exists() }
        val data = asset.readBytes()
        val pages = readOggPages(data)
        val finalPage = pages.last()

        assertEquals(4_101_300L, finalPage.granulePosition)
        assertTrue("final Ogg page must be marked end-of-stream", finalPage.headerType and 0x04 != 0)
    }

    private fun readOggPages(data: ByteArray): List<OggPageInfo> {
        val pages = mutableListOf<OggPageInfo>()
        var offset = 0
        while (offset < data.size) {
            require(data.copyOfRange(offset, offset + 4).decodeToString() == "OggS") {
                "Invalid Ogg page at byte $offset"
            }
            val segmentCount = data[offset + 26].toInt() and 0xff
            var bodyLength = 0
            repeat(segmentCount) { index ->
                bodyLength += data[offset + 27 + index].toInt() and 0xff
            }
            pages += OggPageInfo(
                headerType = data[offset + 5].toInt() and 0xff,
                granulePosition = readLittleEndianLong(data, offset + 6)
            )
            offset += 27 + segmentCount + bodyLength
        }
        return pages
    }

    private fun readLittleEndianLong(data: ByteArray, offset: Int): Long {
        var value = 0L
        repeat(8) { index ->
            value = value or ((data[offset + index].toLong() and 0xffL) shl (index * 8))
        }
        return value
    }

    private data class OggPageInfo(
        val headerType: Int,
        val granulePosition: Long
    )
}
