package com.example.uml_chudadi.profile

import com.example.uml_chudadi.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileControllerTest {
    @Test
    fun levelProgressHandlesUpgradeAndMultiLevelUpgrade() {
        assertEquals(1, ProfileProgress.levelForXp(0))
        assertEquals(2, ProfileProgress.levelForXp(ProfileProgress.xpForLevel(2)))
        assertTrue(ProfileProgress.levelForXp(ProfileProgress.xpForLevel(5) + 5) >= 5)
    }

    @Test
    fun settlementRewardsFirstPlaceMoreThanFourthPlace() {
        val first = ProfileController.settleMatch(
            PlayerProfile(),
            settlement(rank = 1, remainingCards = 0)
        )
        val fourth = ProfileController.settleMatch(
            PlayerProfile(),
            settlement(rank = 4, remainingCards = 9)
        )

        assertTrue(first.coinsDelta > fourth.coinsDelta)
        assertTrue(first.xpDelta > fourth.xpDelta)
        assertEquals(0, first.scoreDelta)
        assertEquals(1, first.profile.stats.wins)
        assertEquals(0, fourth.profile.stats.wins)
    }

    @Test
    fun achievementsUnlockOnlyOnce() {
        val first = ProfileController.settleMatch(PlayerProfile(), settlement(rank = 1, remainingCards = 0))
        val second = ProfileController.settleMatch(first.profile, settlement(rank = 1, remainingCards = 0))

        assertTrue(first.unlockedAchievements.any { it.id == "first_win" })
        assertFalse(second.unlockedAchievements.any { it.id == "first_win" })
    }

    @Test
    fun dailyRewardCanOnlyBeClaimedOncePerDay() {
        val profile = PlayerProfile()
        val first = ProfileController.claimDailyReward(profile, "20260531")
        val second = ProfileController.claimDailyReward(first.profile, "20260531")
        val third = ProfileController.claimDailyReward(first.profile, "20260601")

        assertTrue(first.dailyClaimed)
        assertFalse(second.dailyClaimed)
        assertTrue(third.dailyClaimed)
    }

    @Test
    fun historyCodecRoundTripsChineseAndTrimsToFifty() {
        val records = (1..55).map {
            MatchRecord(
                id = it.toLong(),
                timestamp = 1_000L + it,
                mode = if (it % 2 == 0) GameModeLabel.Bluetooth else GameModeLabel.HumanVsAi,
                difficulty = Difficulty.Hard,
                ruleName = "北方玩法|特殊$it",
                rank = 1,
                winnerName = "牌友, $it",
                remainingCards = 0,
                coinsDelta = 360,
                scoreDelta = 28,
                xpDelta = 120
            )
        }

        val decoded = ProfileCodec.decodeHistory(ProfileCodec.encodeHistory(records))

        assertEquals(50, decoded.size)
        assertEquals("北方玩法|特殊1", decoded.first().ruleName)
        assertEquals("牌友, 1", decoded.first().winnerName)
    }

    @Test
    fun avatarSelectionUsesBuiltInOrGalleryPath() {
        val profile = PlayerProfile(coins = 8_000, xp = ProfileProgress.xpForLevel(8), customAvatarUnlocked = true)
        val unlocked = ProfileController.unlockAvatar(profile, "dragon")
        val builtIn = ProfileController.selectBuiltInAvatar(unlocked.profile, "dragon")
        val gallery = ProfileController.selectGalleryAvatar(builtIn, "/tmp/avatar.jpg")

        assertEquals("dragon", builtIn.avatar.builtInId)
        assertEquals(null, builtIn.avatar.galleryPath)
        assertEquals("/tmp/avatar.jpg", gallery.avatar.galleryPath)
    }

    @Test
    fun renameCostsCoinsOnlyWhenNameChanges() {
        val profile = PlayerProfile(coins = 1_000, nickname = "旧名字")

        val unchanged = ProfileController.renameWithCost(profile, "旧名字")
        val renamed = ProfileController.renameWithCost(profile, "新名字")

        assertEquals(1_000, unchanged.profile.coins)
        assertEquals("旧名字", unchanged.profile.nickname)
        assertEquals(500, renamed.profile.coins)
        assertEquals("新名字", renamed.profile.nickname)
    }

    @Test
    fun renameFailsWhenCoinsAreInsufficient() {
        val profile = PlayerProfile(coins = 100, nickname = "旧名字")

        val result = ProfileController.renameWithCost(profile, "新名字")

        assertTrue(result.failed)
        assertEquals(profile, result.profile)
    }

    @Test
    fun avatarUnlockCostsCoinsAndDoesNotChargeTwice() {
        val profile = PlayerProfile(coins = 2_000)

        val first = ProfileController.unlockAvatar(profile, "heart_star")
        val second = ProfileController.unlockAvatar(first.profile, "heart_star")

        assertEquals(1_200, first.profile.coins)
        assertTrue("heart_star" in first.profile.unlockedAvatars)
        assertEquals(first.profile.coins, second.profile.coins)
    }

    @Test
    fun lockedAvatarCannotBeSelectedBeforeUnlock() {
        val profile = PlayerProfile()

        val selected = ProfileController.selectBuiltInAvatar(profile, "heart_star")

        assertEquals(profile.avatar.builtInId, selected.avatar.builtInId)
    }

    @Test
    fun customAvatarRequiresOneTimeUnlock() {
        val locked = PlayerProfile(coins = 5_000)
        val ignored = ProfileController.selectGalleryAvatar(locked, "/tmp/avatar.jpg")
        val unlocked = ProfileController.unlockCustomAvatar(locked)
        val selected = ProfileController.selectGalleryAvatar(unlocked.profile, "/tmp/avatar.jpg")

        assertEquals(null, ignored.avatar.galleryPath)
        assertEquals(1_000, unlocked.profile.coins)
        assertTrue(unlocked.profile.customAvatarUnlocked)
        assertEquals("/tmp/avatar.jpg", selected.avatar.galleryPath)
    }

    @Test
    fun resetStatsCostsCoinsAndKeepsProgressAndUnlocks() {
        val profile = PlayerProfile(
            coins = 5_000,
            xp = ProfileProgress.xpForLevel(6),
            unlockedAchievements = setOf("first_win"),
            unlockedAvatars = setOf("gold_spade", "heart_star"),
            stats = ProfileStats(totalGames = 12, wins = 7, bluetoothGames = 2, hardWins = 1, winStreak = 3),
            history = listOf(
                MatchRecord(1, 1, GameModeLabel.HumanVsAi, Difficulty.Easy, "北方玩法", 1, "你", 0, 360, 0, 120)
            )
        )

        val reset = ProfileController.resetStats(profile)

        assertEquals(2_500, reset.profile.coins)
        assertEquals(profile.xp, reset.profile.xp)
        assertEquals(profile.unlockedAchievements, reset.profile.unlockedAchievements)
        assertEquals(profile.unlockedAvatars, reset.profile.unlockedAvatars)
        assertEquals(0, reset.profile.stats.totalGames)
        assertTrue(reset.profile.history.isEmpty())
    }

    private fun settlement(rank: Int, remainingCards: Int): MatchSettlement {
        return MatchSettlement(
            timestamp = 1_717_171_717L + rank,
            mode = GameModeLabel.HumanVsAi,
            difficulty = Difficulty.Hard,
            ruleName = "北方玩法",
            rank = rank,
            winnerName = if (rank == 1) "你" else "小北",
            remainingCards = remainingCards
        )
    }
}
