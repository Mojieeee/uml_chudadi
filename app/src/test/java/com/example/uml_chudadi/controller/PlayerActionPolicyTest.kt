package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.NorthRuleSet
import com.example.uml_chudadi.model.PlayedHand
import com.example.uml_chudadi.model.Player
import com.example.uml_chudadi.model.PlayerKind
import com.example.uml_chudadi.model.Rank
import com.example.uml_chudadi.model.Suit
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerActionPolicyTest {
    @Test
    fun noLegalPlayShowsPassHintAndDisablesPlayHint() {
        val state = stateForLocalTurn(
            localHand = listOf(card(Suit.Diamonds, Rank.Four)),
            lastCard = card(Suit.Spades, Rank.Two)
        )

        val availability = PlayerActionPolicy.evaluate(state, localPlayerId = 0)

        assertTrue(availability.canAct)
        assertTrue(availability.canPass)
        assertFalse(availability.hasLegalPlay)
        assertTrue(availability.notice.orEmpty().contains("没有可以大过人家的牌"))
    }

    @Test
    fun playableCardKeepsActionsEnabledWithoutNoPlayHint() {
        val state = stateForLocalTurn(
            localHand = listOf(card(Suit.Spades, Rank.Five)),
            lastCard = card(Suit.Diamonds, Rank.Four)
        )

        val availability = PlayerActionPolicy.evaluate(state, localPlayerId = 0)

        assertTrue(availability.canAct)
        assertTrue(availability.canPass)
        assertTrue(availability.hasLegalPlay)
        assertTrue(availability.notice == null)
    }

    @Test
    fun waitingForHostConfirmationLocksActions() {
        val state = stateForLocalTurn(
            localHand = listOf(card(Suit.Spades, Rank.Five)),
            lastCard = card(Suit.Diamonds, Rank.Four)
        )

        val availability = PlayerActionPolicy.evaluate(state, localPlayerId = 0, inputLocked = true)

        assertFalse(availability.canAct)
        assertFalse(availability.canPass)
        assertTrue(availability.hasLegalPlay)
        assertTrue(availability.notice.orEmpty().contains("房主确认"))
    }

    @Test
    fun playAnimationLocksActionsWithoutShowingProgressText() {
        val state = stateForLocalTurn(
            localHand = listOf(card(Suit.Spades, Rank.Five)),
            lastCard = card(Suit.Diamonds, Rank.Four)
        )

        val availability = PlayerActionPolicy.evaluate(state, localPlayerId = 0, isPlayAnimating = true)

        assertFalse(availability.canAct)
        assertFalse(availability.canPass)
        assertTrue(availability.hasLegalPlay)
        assertTrue(availability.notice == null)
    }

    @Test
    fun notStartedGameDisablesAllLocalActions() {
        val state = stateForLocalTurn(
            localHand = listOf(card(Suit.Spades, Rank.Five)),
            lastCard = card(Suit.Diamonds, Rank.Four)
        )

        val availability = PlayerActionPolicy.evaluate(state, localPlayerId = 0, gameStarted = false)

        assertFalse(availability.canAct)
        assertFalse(availability.canPass)
        assertFalse(availability.hasLegalPlay)
        assertTrue(availability.notice == null)
    }

    private fun stateForLocalTurn(localHand: List<Card>, lastCard: Card): GameState {
        val lastType = requireNotNull(NorthRuleSet.classify(listOf(lastCard)))
        return GameState(
            players = listOf(
                Player(0, "你", PlayerKind.Human, localHand),
                Player(1, "好友1", PlayerKind.Remote, listOf(card(Suit.Clubs, Rank.Five))),
                Player(2, "好友2", PlayerKind.Remote, listOf(card(Suit.Hearts, Rank.Six))),
                Player(3, "人机", PlayerKind.LocalAi, listOf(card(Suit.Spades, Rank.Seven)))
            ),
            ruleSet = NorthRuleSet,
            currentPlayerId = 0,
            lastPlayedHand = PlayedHand(1, lastType),
            firstTurn = false
        )
    }

    private fun card(suit: Suit, rank: Rank): Card = Card(suit, rank)
}
