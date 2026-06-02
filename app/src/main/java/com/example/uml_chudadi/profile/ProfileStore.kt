package com.example.uml_chudadi.profile

import android.content.Context
import android.content.SharedPreferences
import com.example.uml_chudadi.model.Difficulty
import java.net.URLDecoder
import java.net.URLEncoder

class ProfileStore(private val context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): PlayerProfile {
        val legacy = contextLegacyDefaults(preferences)
        val stats = ProfileStats(
            totalGames = preferences.getInt(KEY_TOTAL_GAMES, legacy.stats.totalGames),
            wins = preferences.getInt(KEY_WINS, legacy.stats.wins),
            bluetoothGames = preferences.getInt(KEY_BLUETOOTH_GAMES, legacy.stats.bluetoothGames),
            bluetoothWins = preferences.getInt(KEY_BLUETOOTH_WINS, legacy.stats.bluetoothWins),
            hardWins = preferences.getInt(KEY_HARD_WINS, legacy.stats.hardWins),
            winStreak = preferences.getInt(KEY_WIN_STREAK, legacy.stats.winStreak),
            bestWinRemaining = preferences.getInt(KEY_BEST_WIN_REMAINING, legacy.stats.bestWinRemaining)
        )
        val avatar = AvatarProfile(
            builtInId = preferences.getString(KEY_AVATAR_BUILT_IN, legacy.avatar.builtInId) ?: legacy.avatar.builtInId,
            galleryPath = preferences.getString(KEY_AVATAR_PATH, legacy.avatar.galleryPath),
            borderId = preferences.getString(KEY_AVATAR_BORDER, legacy.avatar.borderId) ?: legacy.avatar.borderId
        )
        val unlockedAvatars = preferences.getStringSet(KEY_UNLOCKED_AVATARS, emptySet()).orEmpty() +
            AvatarCatalog.freeAvatarIds +
            avatar.builtInId
        return PlayerProfile(
            nickname = preferences.getString(KEY_NICKNAME, legacy.nickname).orEmpty().ifBlank { "牌桌新星" },
            coins = preferences.getInt(KEY_COINS, legacy.coins),
            score = preferences.getInt(KEY_SCORE, legacy.score),
            xp = preferences.getInt(KEY_XP, legacy.xp),
            avatar = avatar,
            stats = stats,
            unlockedAchievements = preferences.getStringSet(KEY_ACHIEVEMENTS, emptySet()).orEmpty(),
            unlockedAvatars = unlockedAvatars,
            customAvatarUnlocked = preferences.getBoolean(KEY_CUSTOM_AVATAR_UNLOCKED, false),
            history = ProfileCodec.decodeHistory(preferences.getString(KEY_HISTORY, "").orEmpty()),
            lastDailyRewardDate = preferences.getString(KEY_DAILY_DATE, "").orEmpty()
        )
    }

    fun save(profile: PlayerProfile) {
        preferences.edit()
            .putString(KEY_NICKNAME, profile.nickname)
            .putInt(KEY_COINS, profile.coins)
            .putInt(KEY_SCORE, profile.score)
            .putInt(KEY_XP, profile.xp)
            .putString(KEY_AVATAR_BUILT_IN, profile.avatar.builtInId)
            .putString(KEY_AVATAR_PATH, profile.avatar.galleryPath)
            .putString(KEY_AVATAR_BORDER, profile.avatar.borderId)
            .putStringSet(KEY_UNLOCKED_AVATARS, profile.unlockedAvatars + AvatarCatalog.freeAvatarIds + profile.avatar.builtInId)
            .putBoolean(KEY_CUSTOM_AVATAR_UNLOCKED, profile.customAvatarUnlocked)
            .putInt(KEY_TOTAL_GAMES, profile.stats.totalGames)
            .putInt(KEY_WINS, profile.stats.wins)
            .putInt(KEY_BLUETOOTH_GAMES, profile.stats.bluetoothGames)
            .putInt(KEY_BLUETOOTH_WINS, profile.stats.bluetoothWins)
            .putInt(KEY_HARD_WINS, profile.stats.hardWins)
            .putInt(KEY_WIN_STREAK, profile.stats.winStreak)
            .putInt(KEY_BEST_WIN_REMAINING, profile.stats.bestWinRemaining)
            .putStringSet(KEY_ACHIEVEMENTS, profile.unlockedAchievements)
            .putString(KEY_HISTORY, ProfileCodec.encodeHistory(profile.history))
            .putString(KEY_DAILY_DATE, profile.lastDailyRewardDate)
            .apply()
    }

    private fun contextLegacyDefaults(preferences: SharedPreferences): PlayerProfile {
        return PlayerProfile(
            coins = preferences.getInt("coins", 12_880),
            stats = ProfileStats(
                totalGames = preferences.getInt("total_games", 0),
                wins = preferences.getInt("wins", 0),
                bluetoothGames = preferences.getInt("bluetooth_games", 0),
                winStreak = preferences.getInt("win_streak", 0),
                bestWinRemaining = preferences.getInt("best_win_hands", 0)
            )
        )
    }

    companion object {
        private const val PROFILE_PREFS_NAME = "chudadi_profile"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_COINS = "coins"
        private const val KEY_SCORE = "score"
        private const val KEY_XP = "xp"
        private const val KEY_AVATAR_BUILT_IN = "avatar_built_in"
        private const val KEY_AVATAR_PATH = "avatar_path"
        private const val KEY_AVATAR_BORDER = "avatar_border"
        private const val KEY_UNLOCKED_AVATARS = "unlocked_avatars"
        private const val KEY_CUSTOM_AVATAR_UNLOCKED = "custom_avatar_unlocked"
        private const val KEY_TOTAL_GAMES = "total_games"
        private const val KEY_WINS = "wins"
        private const val KEY_BLUETOOTH_GAMES = "bluetooth_games"
        private const val KEY_BLUETOOTH_WINS = "bluetooth_wins"
        private const val KEY_HARD_WINS = "hard_wins"
        private const val KEY_WIN_STREAK = "win_streak"
        private const val KEY_BEST_WIN_REMAINING = "best_win_remaining"
        private const val KEY_ACHIEVEMENTS = "achievements"
        private const val KEY_HISTORY = "history"
        private const val KEY_DAILY_DATE = "daily_date"
    }
}

object ProfileCodec {
    fun encodeHistory(history: List<MatchRecord>): String {
        return history.take(50).joinToString("\n") { encodeRecord(it) }
    }

    fun decodeHistory(raw: String): List<MatchRecord> {
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .mapNotNull { decodeRecord(it) }
            .take(50)
            .toList()
    }

    fun encodeRecord(record: MatchRecord): String {
        return listOf(
            record.id.toString(),
            record.timestamp.toString(),
            record.mode.name,
            record.difficulty?.name.orEmpty(),
            e(record.ruleName),
            record.rank.toString(),
            e(record.winnerName),
            record.remainingCards.toString(),
            record.coinsDelta.toString(),
            record.scoreDelta.toString(),
            record.xpDelta.toString()
        ).joinToString("|")
    }

    fun decodeRecord(raw: String): MatchRecord? {
        val parts = raw.split("|")
        if (parts.size < 11) return null
        return runCatching {
            MatchRecord(
                id = parts[0].toLong(),
                timestamp = parts[1].toLong(),
                mode = GameModeLabel.valueOf(parts[2]),
                difficulty = parts[3].takeIf { it.isNotBlank() }?.let { Difficulty.valueOf(it) },
                ruleName = d(parts[4]),
                rank = parts[5].toInt(),
                winnerName = d(parts[6]),
                remainingCards = parts[7].toInt(),
                coinsDelta = parts[8].toInt(),
                scoreDelta = parts[9].toInt(),
                xpDelta = parts[10].toInt()
            )
        }.getOrNull()
    }

    private fun e(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun d(value: String): String = URLDecoder.decode(value, "UTF-8")
}
