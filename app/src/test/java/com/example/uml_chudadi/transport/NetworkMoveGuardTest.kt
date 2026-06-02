package com.example.uml_chudadi.transport

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

class NetworkMoveGuardTest {
    @Test
    fun hostAcceptsOnlyCurrentRemotePlayerRequest() {
        val state = networkState(currentPlayerId = 1, playerOneKind = PlayerKind.Remote)

        val request = GameMessage.MoveRequest(1, listOf(card(Suit.Diamonds, Rank.Four)), pass = false)

        assertTrue(NetworkMoveGuard.canHostAcceptMove(state, request))
    }

    @Test
    fun hostRejectsStaleDuplicateRequestAfterTurnMovedOn() {
        val state = networkState(currentPlayerId = 2, playerOneKind = PlayerKind.Remote)

        val duplicate = GameMessage.MoveRequest(1, listOf(card(Suit.Diamonds, Rank.Four)), pass = false)

        assertFalse(NetworkMoveGuard.canHostAcceptMove(state, duplicate))
    }

    @Test
    fun hostRejectsRequestsForLocalHostOrAiSeats() {
        val hostTurn = networkState(currentPlayerId = 0, playerOneKind = PlayerKind.Remote)
        val aiTurn = networkState(currentPlayerId = 3, playerOneKind = PlayerKind.Remote)

        assertFalse(NetworkMoveGuard.canHostAcceptMove(hostTurn, GameMessage.MoveRequest(0, emptyList(), pass = true)))
        assertFalse(NetworkMoveGuard.canHostAcceptMove(aiTurn, GameMessage.MoveRequest(3, emptyList(), pass = true)))
    }

    @Test
    fun messageOnlyInvalidPlayIsNotAcceptedAsAppliedMove() {
        val previous = networkState(currentPlayerId = 1, playerOneKind = PlayerKind.Remote)
        val invalid = previous.copy(message = "牌型不合法")

        assertFalse(
            NetworkMoveGuard.wasMoveApplied(
                previous = previous,
                next = invalid,
                playerId = 1,
                cards = listOf(card(Suit.Diamonds, Rank.Three)),
                pass = false
            )
        )
    }

    @Test
    fun realPassAndRealPlayAreRecognizedAsAppliedMoves() {
        val controller = com.example.uml_chudadi.controller.GameController(NorthRuleSet)
        val lastType = requireNotNull(NorthRuleSet.classify(listOf(card(Suit.Diamonds, Rank.Four))))
        val passPrevious = networkState(currentPlayerId = 1, playerOneKind = PlayerKind.Remote)
            .copy(lastPlayedHand = PlayedHand(0, lastType), passCount = 0)
        val passNext = controller.pass(passPrevious, 1)
        val playPrevious = networkState(currentPlayerId = 1, playerOneKind = PlayerKind.Remote)
            .copy(firstTurn = false, lastPlayedHand = null)
        val playCards = listOf(card(Suit.Diamonds, Rank.Four))
        val playNext = controller.play(playPrevious, 1, playCards)

        assertTrue(NetworkMoveGuard.wasMoveApplied(passPrevious, passNext, 1, emptyList(), pass = true))
        assertTrue(NetworkMoveGuard.wasMoveApplied(playPrevious, playNext, 1, playCards, pass = false))
    }

    private fun networkState(currentPlayerId: Int, playerOneKind: PlayerKind): GameState {
        return GameState(
            players = listOf(
                Player(0, "房主", PlayerKind.Human, listOf(card(Suit.Clubs, Rank.Three))),
                Player(1, "好友1", playerOneKind, listOf(card(Suit.Diamonds, Rank.Four))),
                Player(2, "好友2", PlayerKind.Remote, listOf(card(Suit.Hearts, Rank.Five))),
                Player(3, "人机", PlayerKind.LocalAi, listOf(card(Suit.Spades, Rank.Six)))
            ),
            ruleSet = NorthRuleSet,
            currentPlayerId = currentPlayerId,
            firstTurn = false
        )
    }

    private fun card(suit: Suit, rank: Rank): Card = Card(suit, rank)
}
