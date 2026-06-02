package com.example.uml_chudadi.profile

import com.example.uml_chudadi.model.Difficulty

object AchievementCatalog {
    val all = listOf(
        AchievementDefinition("first_win", "初尝胜利", "赢下第一局", 180, 40),
        AchievementDefinition("streak_3", "三连胜", "连续赢下三局", 320, 70),
        AchievementDefinition("veteran_10", "十局老手", "完成十局对战", 240, 60),
        AchievementDefinition("hard_win", "困难破局", "在人机困难中获胜", 300, 80),
        AchievementDefinition("bluetooth_first", "好友同桌", "完成一局蓝牙对局", 220, 50),
        AchievementDefinition("bluetooth_win", "同桌赢家", "赢下一局蓝牙对局", 360, 90),
        AchievementDefinition("zero_hand", "清台收官", "以零张手牌结束并获胜", 260, 60),
        AchievementDefinition("coin_5000", "小有积蓄", "金币达到 5000", 200, 45),
        AchievementDefinition("level_5", "等级 5", "达到 5 级", 260, 70),
        AchievementDefinition("level_10", "等级 10", "达到 10 级", 420, 110),
        AchievementDefinition("level_20", "等级 20", "达到 20 级", 900, 220)
    )

    fun byId(id: String): AchievementDefinition? = all.firstOrNull { it.id == id }
}

object ProfileController {
    const val RENAME_COST = 500
    const val CUSTOM_AVATAR_COST = 4_000
    const val RESET_STATS_COST = 2_500

    private val coinByRank = mapOf(1 to 360, 2 to 120, 3 to -60, 4 to -120)
    private val xpByRank = mapOf(1 to 120, 2 to 85, 3 to 62, 4 to 48)

    fun settleMatch(profile: PlayerProfile, settlement: MatchSettlement): ProfileChange {
        val rank = settlement.rank.coerceIn(1, 4)
        val won = rank == 1
        val modeBonusXp = if (settlement.mode == GameModeLabel.Bluetooth) 25 else 0
        val difficultyBonusXp = when (settlement.difficulty) {
            Difficulty.Hard -> if (won) 35 else 12
            Difficulty.Normal -> 8
            else -> 0
        }
        val coinsDelta = coinByRank.getValue(rank) + if (settlement.mode == GameModeLabel.Bluetooth && won) 80 else 0
        val scoreDelta = 0
        val xpDelta = xpByRank.getValue(rank) + modeBonusXp + difficultyBonusXp
        val oldLevel = profile.level
        val stats = profile.stats.copy(
            totalGames = profile.stats.totalGames + 1,
            wins = profile.stats.wins + if (won) 1 else 0,
            bluetoothGames = profile.stats.bluetoothGames + if (settlement.mode == GameModeLabel.Bluetooth) 1 else 0,
            bluetoothWins = profile.stats.bluetoothWins + if (settlement.mode == GameModeLabel.Bluetooth && won) 1 else 0,
            hardWins = profile.stats.hardWins + if (settlement.difficulty == Difficulty.Hard && won) 1 else 0,
            winStreak = if (won) profile.stats.winStreak + 1 else 0,
            bestWinRemaining = if (won) {
                if (profile.stats.bestWinRemaining == 0) settlement.remainingCards else minOf(profile.stats.bestWinRemaining, settlement.remainingCards)
            } else {
                profile.stats.bestWinRemaining
            }
        )
        val record = MatchRecord(
            id = settlement.timestamp,
            timestamp = settlement.timestamp,
            mode = settlement.mode,
            difficulty = settlement.difficulty,
            ruleName = settlement.ruleName,
            rank = rank,
            winnerName = settlement.winnerName,
            remainingCards = settlement.remainingCards,
            coinsDelta = coinsDelta,
            scoreDelta = scoreDelta,
            xpDelta = xpDelta
        )
        val baseProfile = profile.copy(
            coins = (profile.coins + coinsDelta).coerceAtLeast(0),
            xp = profile.xp + xpDelta,
            stats = stats,
            history = (listOf(record) + profile.history).take(50)
        )
        val unlocked = newlyUnlocked(baseProfile, record)
        val rewardCoins = unlocked.sumOf { it.coinReward }
        val rewardXp = unlocked.sumOf { it.xpReward }
        val finalProfile = baseProfile.copy(
            coins = baseProfile.coins + rewardCoins,
            xp = baseProfile.xp + rewardXp,
            unlockedAchievements = baseProfile.unlockedAchievements + unlocked.map { it.id }
        )
        return ProfileChange(
            profile = finalProfile,
            record = record,
            coinsDelta = coinsDelta + rewardCoins,
            scoreDelta = scoreDelta,
            xpDelta = xpDelta + rewardXp,
            oldLevel = oldLevel,
            newLevel = finalProfile.level,
            unlockedAchievements = unlocked,
            message = "本局结算完成"
        )
    }

    fun canClaimDaily(profile: PlayerProfile, todayKey: String): Boolean {
        return profile.lastDailyRewardDate != todayKey
    }

    fun claimDailyReward(profile: PlayerProfile, todayKey: String): ProfileChange {
        if (!canClaimDaily(profile, todayKey)) {
            return ProfileChange(profile = profile)
        }
        val oldLevel = profile.level
        val updated = profile.copy(
            coins = profile.coins + 220,
            xp = profile.xp + 45,
            lastDailyRewardDate = todayKey
        )
        return ProfileChange(
            profile = updated,
            coinsDelta = 220,
            xpDelta = 45,
            oldLevel = oldLevel,
            newLevel = updated.level,
            dailyClaimed = true,
            message = "每日奖励已领取 +220 金币"
        )
    }

    fun rename(profile: PlayerProfile, nickname: String): PlayerProfile {
        val clean = nickname.trim().take(10).ifBlank { "牌桌新星" }
        return profile.copy(nickname = clean)
    }

    fun renameWithCost(profile: PlayerProfile, nickname: String): ProfileChange {
        val clean = nickname.trim().take(10).ifBlank { "牌桌新星" }
        if (clean == profile.nickname) {
            return ProfileChange(profile = profile, message = "昵称没有变化")
        }
        if (profile.coins < RENAME_COST) {
            return ProfileChange(profile = profile, message = "金币不足，改名需要 $RENAME_COST 金币", failed = true)
        }
        val updated = profile.copy(nickname = clean, coins = profile.coins - RENAME_COST)
        return ProfileChange(
            profile = updated,
            coinsDelta = -RENAME_COST,
            message = "昵称已更新 -$RENAME_COST 金币"
        )
    }

    fun selectBuiltInAvatar(profile: PlayerProfile, avatarId: String): PlayerProfile {
        val avatar = AvatarCatalog.byId(avatarId)
        return if (avatar.id in normalizedUnlockedAvatars(profile)) {
            profile.copy(avatar = profile.avatar.copy(builtInId = avatar.id, galleryPath = null))
        } else {
            profile
        }
    }

    fun unlockAvatar(profile: PlayerProfile, avatarId: String): ProfileChange {
        val avatar = AvatarCatalog.byId(avatarId)
        val unlocked = normalizedUnlockedAvatars(profile)
        if (avatar.id in unlocked) {
            val updated = selectBuiltInAvatar(profile, avatar.id)
            return ProfileChange(profile = updated, message = "已使用${avatar.label}")
        }
        if (profile.level < avatar.unlockLevel) {
            return ProfileChange(profile = profile, message = "达到 Lv.${avatar.unlockLevel} 后可解锁", failed = true)
        }
        if (profile.coins < avatar.price) {
            return ProfileChange(profile = profile, message = "金币不足，解锁需要 ${avatar.price} 金币", failed = true)
        }
        val updated = profile.copy(
            coins = profile.coins - avatar.price,
            unlockedAvatars = unlocked + avatar.id,
            avatar = profile.avatar.copy(builtInId = avatar.id, galleryPath = null)
        )
        return ProfileChange(
            profile = updated,
            coinsDelta = -avatar.price,
            message = "已解锁${avatar.label} -${avatar.price} 金币"
        )
    }

    fun selectGalleryAvatar(profile: PlayerProfile, path: String): PlayerProfile {
        return if (profile.customAvatarUnlocked) {
            profile.copy(avatar = profile.avatar.copy(galleryPath = path))
        } else {
            profile
        }
    }

    fun unlockCustomAvatar(profile: PlayerProfile): ProfileChange {
        if (profile.customAvatarUnlocked) {
            return ProfileChange(profile = profile, message = "自定义头像已解锁")
        }
        if (profile.coins < CUSTOM_AVATAR_COST) {
            return ProfileChange(profile = profile, message = "金币不足，解锁自定义头像需要 $CUSTOM_AVATAR_COST 金币", failed = true)
        }
        val updated = profile.copy(coins = profile.coins - CUSTOM_AVATAR_COST, customAvatarUnlocked = true)
        return ProfileChange(
            profile = updated,
            coinsDelta = -CUSTOM_AVATAR_COST,
            message = "自定义头像已解锁 -$CUSTOM_AVATAR_COST 金币"
        )
    }

    fun resetStats(profile: PlayerProfile): ProfileChange {
        if (profile.coins < RESET_STATS_COST) {
            return ProfileChange(profile = profile, message = "金币不足，重置战绩需要 $RESET_STATS_COST 金币", failed = true)
        }
        val updated = profile.copy(
            coins = profile.coins - RESET_STATS_COST,
            stats = ProfileStats(),
            history = emptyList()
        )
        return ProfileChange(
            profile = updated,
            coinsDelta = -RESET_STATS_COST,
            message = "战绩已重置 -$RESET_STATS_COST 金币"
        )
    }

    fun normalizedUnlockedAvatars(profile: PlayerProfile): Set<String> {
        return profile.unlockedAvatars + AvatarCatalog.freeAvatarIds + profile.avatar.builtInId
    }

    private fun newlyUnlocked(profile: PlayerProfile, latestRecord: MatchRecord): List<AchievementDefinition> {
        return AchievementCatalog.all.filter { achievement ->
            achievement.id !in profile.unlockedAchievements && qualifies(achievement.id, profile, latestRecord)
        }
    }

    private fun qualifies(id: String, profile: PlayerProfile, latestRecord: MatchRecord): Boolean {
        return when (id) {
            "first_win" -> profile.stats.wins >= 1
            "streak_3" -> profile.stats.winStreak >= 3
            "veteran_10" -> profile.stats.totalGames >= 10
            "hard_win" -> profile.stats.hardWins >= 1
            "bluetooth_first" -> profile.stats.bluetoothGames >= 1
            "bluetooth_win" -> profile.stats.bluetoothWins >= 1
            "zero_hand" -> latestRecord.rank == 1 && latestRecord.remainingCards == 0
            "coin_5000" -> profile.coins >= 5_000
            "level_5" -> profile.level >= 5
            "level_10" -> profile.level >= 10
            "level_20" -> profile.level >= 20
            else -> false
        }
    }
}
