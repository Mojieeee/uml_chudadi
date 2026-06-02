package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.HandCategory
import com.example.uml_chudadi.model.Move
import com.example.uml_chudadi.model.NorthRuleSet
import com.example.uml_chudadi.model.Player
import com.example.uml_chudadi.model.PlayerKind
import com.example.uml_chudadi.model.Rank
import com.example.uml_chudadi.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameControllerTest {
    @Test
    fun newGameStartsFromRuleFirstCardHolder() {
        val controller = GameController(NorthRuleSet)
        val state = controller.newGame(seed = 7)

        assertTrue(NorthRuleSet.firstCard in state.currentPlayer.hand)
    }

    @Test
    fun firstPlayMustContainFirstCard() {
        val controller = GameController(NorthRuleSet)
        val state = controller.newGame(seed = 7)
        val wrongCard = state.currentPlayer.hand.first { it != NorthRuleSet.firstCard }

        val result = controller.play(state, state.currentPlayerId, listOf(wrongCard))

        assertEquals(state.currentPlayerId, result.currentPlayerId)
        assertTrue(result.message.contains("首出"))
    }

    @Test
    fun greedyAndHardStrategiesReturnLegalMoves() {
        val controller = GameController(NorthRuleSet)
        val state = controller.newGame(seed = 12)
        val greedy = GreedyAiStrategy(controller)
        val normal = HeuristicAiStrategy(controller)
        val hard = MonteCarloRolloutAiStrategy(controller, rollouts = 8)
        val aiPlayer = state.players.first { it.id != state.currentPlayerId }
        val aiState = state.copy(currentPlayerId = aiPlayer.id, lastPlayedHand = null, firstTurn = false)

        assertTrue(greedy.chooseMove(aiState, aiPlayer.id) is Move.Play)
        assertTrue(normal.chooseMove(aiState, aiPlayer.id) is Move.Play)
        assertTrue(hard.chooseMove(aiState, aiPlayer.id) is Move.Play)
    }

    @Test
    fun hardStrategyWinsImmediatelyWhenPossible() {
        val controller = GameController(NorthRuleSet)
        val winningCard = Card(Suit.Spades, Rank.Ace)
        val state = GameState(
            players = listOf(
                Player(0, "你", PlayerKind.Human, listOf(Card(Suit.Diamonds, Rank.Four))),
                Player(1, "小北", PlayerKind.LocalAi, listOf(winningCard)),
                Player(2, "阿豪", PlayerKind.LocalAi, listOf(Card(Suit.Clubs, Rank.Five))),
                Player(3, "星河", PlayerKind.LocalAi, listOf(Card(Suit.Hearts, Rank.Six)))
            ),
            ruleSet = NorthRuleSet,
            currentPlayerId = 1,
            firstTurn = false
        )

        val move = MonteCarloRolloutAiStrategy(controller, rollouts = 8).chooseMove(state, 1)

        assertEquals(Move.Play(1, listOf(winningCard)), move)
    }

    @Test
    fun hardStrategyAvoidsUnnecessaryBombLead() {
        val controller = GameController(NorthRuleSet)
        val state = GameState(
            players = listOf(
                Player(0, "你", PlayerKind.Human, listOf(Card(Suit.Diamonds, Rank.Four))),
                Player(
                    1,
                    "小北",
                    PlayerKind.LocalAi,
                    listOf(
                        Card(Suit.Diamonds, Rank.Three),
                        Card(Suit.Clubs, Rank.Three),
                        Card(Suit.Hearts, Rank.Three),
                        Card(Suit.Spades, Rank.Three),
                        Card(Suit.Diamonds, Rank.Four),
                        Card(Suit.Diamonds, Rank.Five),
                        Card(Suit.Clubs, Rank.Six),
                        Card(Suit.Hearts, Rank.Seven),
                        Card(Suit.Spades, Rank.Eight)
                    ).sorted()
                ),
                Player(2, "阿豪", PlayerKind.LocalAi, listOf(Card(Suit.Clubs, Rank.Five))),
                Player(3, "星河", PlayerKind.LocalAi, listOf(Card(Suit.Hearts, Rank.Six)))
            ),
            ruleSet = NorthRuleSet,
            currentPlayerId = 1,
            firstTurn = false
        )

        val move = MonteCarloRolloutAiStrategy(controller, rollouts = 8).chooseMove(state, 1) as Move.Play
        val category = NorthRuleSet.classify(move.cards)?.category

        assertTrue(category != HandCategory.FourWithOne && category != HandCategory.StraightFlush)
    }

    @Test
    fun passCycleReturnsLeadToLastPlayer() {
        val controller = GameController(NorthRuleSet)
        val started = controller.newGame(seed = 7)
        val starter = started.currentPlayerId
        val firstCard = Card(Suit.Spades, Rank.Three)
        val afterPlay = controller.play(started, starter, listOf(firstCard))
        val afterPasses = listOf(afterPlay.currentPlayerId, afterPlay.nextPlayerId(), afterPlay.nextPlayerId(afterPlay.nextPlayerId()))
            .fold(afterPlay) { state, playerId -> controller.pass(state, playerId) }

        assertEquals(starter, afterPasses.currentPlayerId)
        assertEquals(null, afterPasses.lastPlayedHand)
    }

    @Test
    fun randomGreedyGamesAlwaysFinish() {
        repeat(200) { seed ->
            val controller = GameController(NorthRuleSet)
            var state = controller.newGame(seed = seed)
            repeat(260) {
                if (state.isFinished) return@repeat
                val playerId = state.currentPlayerId
                val move = GreedyAiStrategy(controller).chooseMove(state, playerId)
                state = controller.applyMove(state, move)
            }
            assertTrue("seed $seed did not finish", state.isFinished)
        }
    }
}
