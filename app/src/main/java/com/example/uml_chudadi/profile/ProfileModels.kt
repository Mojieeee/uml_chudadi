package com.example.uml_chudadi.profile

import com.example.uml_chudadi.model.Difficulty

enum class GameModeLabel(val title: String) {
    HumanVsAi("人机对局"),
    Bluetooth("好友蓝牙")
}

enum class AvatarRarity(val title: String) {
    Free("免费"),
    Common("普通"),
    Rare("稀有"),
    Legendary("传说")
}

data class BuiltInAvatar(
    val id: String,
    val label: String,
    val mark: String,
    val price: Int,
    val rarity: AvatarRarity,
    val unlockLevel: Int = 1,
    val style: String = id
)

object AvatarCatalog {
    val all = listOf(
        BuiltInAvatar("gold_spade", "黑桃王牌", "♠", 0, AvatarRarity.Free),
        BuiltInAvatar("heart_star", "红心新星", "♥", 800, AvatarRarity.Common),
        BuiltInAvatar("club_master", "梅花高手", "♣", 800, AvatarRarity.Common),
        BuiltInAvatar("diamond_flash", "方块闪电", "♦", 800, AvatarRarity.Common),
        BuiltInAvatar("moon", "月光牌手", "月", 1_800, AvatarRarity.Rare, unlockLevel = 3),
        BuiltInAvatar("sun", "金色赢家", "日", 1_800, AvatarRarity.Rare, unlockLevel = 4),
        BuiltInAvatar("dragon", "龙纹牌客", "龙", 3_200, AvatarRarity.Legendary, unlockLevel = 6),
        BuiltInAvatar("crown", "王冠大师", "冠", 3_200, AvatarRarity.Legendary, unlockLevel = 8)
    )

    fun byId(id: String): BuiltInAvatar = all.firstOrNull { it.id == id } ?: all.first()

    val freeAvatarIds: Set<String> = all.filter { it.price == 0 }.map { it.id }.toSet()
}

data class AvatarProfile(
    val builtInId: String = AvatarCatalog.all.first().id,
    val galleryPath: String? = null,
    val borderId: String = "classic"
)

data class ProfileStats(
    val totalGames: Int = 0,
    val wins: Int = 0,
    val bluetoothGames: Int = 0,
    val bluetoothWins: Int = 0,
    val hardWins: Int = 0,
    val winStreak: Int = 0,
    val bestWinRemaining: Int = 0
) {
    val winRate: Int get() = if (totalGames == 0) 0 else wins * 100 / totalGames
}

data class PlayerProfile(
    val nickname: String = "牌桌新星",
    val coins: Int = 12_880,
    val score: Int = 1_000,
    val xp: Int = 0,
    val avatar: AvatarProfile = AvatarProfile(),
    val stats: ProfileStats = ProfileStats(),
    val unlockedAchievements: Set<String> = emptySet(),
    val unlockedAvatars: Set<String> = AvatarCatalog.freeAvatarIds,
    val customAvatarUnlocked: Boolean = false,
    val history: List<MatchRecord> = emptyList(),
    val lastDailyRewardDate: String = ""
) {
    val level: Int get() = ProfileProgress.levelForXp(xp)
    val title: String get() = ProfileProgress.titleFor(level, stats)
}

data class MatchRecord(
    val id: Long,
    val timestamp: Long,
    val mode: GameModeLabel,
    val difficulty: Difficulty?,
    val ruleName: String,
    val rank: Int,
    val winnerName: String,
    val remainingCards: Int,
    val coinsDelta: Int,
    val scoreDelta: Int,
    val xpDelta: Int
)

data class MatchSettlement(
    val timestamp: Long,
    val mode: GameModeLabel,
    val difficulty: Difficulty?,
    val ruleName: String,
    val rank: Int,
    val winnerName: String,
    val remainingCards: Int
)

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val coinReward: Int,
    val xpReward: Int
)

data class ProfileChange(
    val profile: PlayerProfile,
    val record: MatchRecord? = null,
    val coinsDelta: Int = 0,
    val scoreDelta: Int = 0,
    val xpDelta: Int = 0,
    val oldLevel: Int = profile.level,
    val newLevel: Int = profile.level,
    val unlockedAchievements: List<AchievementDefinition> = emptyList(),
    val dailyClaimed: Boolean = false,
    val message: String = "",
    val failed: Boolean = false
) {
    val leveledUp: Boolean get() = newLevel > oldLevel
}

object ProfileProgress {
    fun xpNeededForNextLevel(level: Int): Int = 80 + level.coerceAtLeast(1) * 45

    fun xpForLevel(level: Int): Int {
        if (level <= 1) return 0
        return (1 until level).sumOf { xpNeededForNextLevel(it) }
    }

    fun levelForXp(xp: Int): Int {
        var level = 1
        while (xp >= xpForLevel(level + 1)) {
            level += 1
        }
        return level
    }

    fun progressToNextLevel(xp: Int): Pair<Int, Int> {
        val level = levelForXp(xp)
        val current = xpForLevel(level)
        val next = xpForLevel(level + 1)
        return (xp - current).coerceAtLeast(0) to (next - current).coerceAtLeast(1)
    }

    fun titleFor(level: Int, stats: ProfileStats): String = when {
        level >= 25 || stats.wins >= 80 -> "锄地宗师"
        level >= 18 || stats.wins >= 45 -> "牌桌大师"
        level >= 12 || stats.winStreak >= 8 -> "控牌高手"
        level >= 6 -> "稳健牌手"
        else -> "牌桌新星"
    }
}
