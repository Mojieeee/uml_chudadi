package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.HandCategory
import com.example.uml_chudadi.model.Move
import kotlin.math.max
import kotlin.random.Random

interface AiStrategy {
    val name: String
    fun chooseMove(state: GameState, playerId: Int): Move
}

class AiController(
    private val gameController: GameController,
    private val strategy: AiStrategy
) {
    val strategyName: String = strategy.name

    fun playTurnIfNeeded(state: GameState): Move? {
        val player = state.currentPlayer
        if (player.kind.name != "LocalAi" || state.isFinished) return null
        return strategy.chooseMove(state, player.id)
    }

    fun applyAiTurns(state: GameState, limit: Int = 12): GameState {
        var nextState = state
        repeat(limit) {
            val move = playTurnIfNeeded(nextState) ?: return nextState
            nextState = gameController.applyMove(nextState, move)
        }
        return nextState
    }
}

class GreedyAiStrategy(private val gameController: GameController) : AiStrategy {
    override val name: String = "简单"

    override fun chooseMove(state: GameState, playerId: Int): Move {
        val play = gameController.legalPlays(state, playerId).minWithOrNull(
            compareBy<List<Card>> { it.size }
                .thenBy { cards -> state.ruleSet.classify(cards)?.category?.strength ?: Int.MAX_VALUE }
                .thenBy { cards -> cards.maxOrNull() }
        )
        return if (play == null) Move.Pass(playerId) else Move.Play(playerId, play)
    }
}

class HeuristicAiStrategy(private val gameController: GameController) : AiStrategy {
    override val name: String = "普通"

    override fun chooseMove(state: GameState, playerId: Int): Move {
        val legal = gameController.legalPlays(state, playerId)
        if (legal.isEmpty()) return Move.Pass(playerId)
        val hand = state.player(playerId).hand
        legal.firstOrNull { it.size == hand.size }?.let { return Move.Play(playerId, it) }

        val pressure = state.players.filterNot { it.id == playerId }.minOf { it.hand.size }
        val best = legal.maxBy { cards ->
            val type = state.ruleSet.classify(cards)
            var score = 500.0
            score += cards.size * if (pressure <= 3) 42.0 else 24.0
            score -= (hand.size - cards.size) * 18.0
            score -= breakPenalty(hand, cards) * 40.0
            score -= if (type?.category == HandCategory.FourWithOne || type?.category == HandCategory.StraightFlush) 260.0 else 0.0
            score -= (cards.maxOrNull()?.rank?.power ?: 0) * 3.0
            score += (type?.category?.strength ?: 0) * 8.0
            score
        }
        return Move.Play(playerId, best)
    }

    private fun breakPenalty(hand: List<Card>, cards: List<Card>): Int {
        val rankGroups = hand.groupBy { it.rank }
        return cards.sumOf { card ->
            val groupSize = rankGroups[card.rank]?.size ?: 1
            if (groupSize >= 2) groupSize else 0
        }
    }
}

class MonteCarloRolloutAiStrategy(
    private val gameController: GameController,
    private val rollouts: Int = 24
) : AiStrategy {
    override val name: String = "困难"

    override fun chooseMove(state: GameState, playerId: Int): Move {
        val legal = gameController.legalPlays(state, playerId)
        if (legal.isEmpty()) return Move.Pass(playerId)

        val hand = state.player(playerId).hand
        legal.firstOrNull { it.size == hand.size }?.let { return Move.Play(playerId, it) }

        val seed = (state.message.hashCode() * 31) + hand.sumOf { it.rank.power * 5 + it.suit.power } + playerId
        val random = Random(seed)
        val best = legal.maxBy { cards ->
            (0 until rollouts).sumOf {
                rolloutScore(state, playerId, cards, random)
            } / rollouts.toDouble()
        }
        return Move.Play(playerId, best)
    }

    private fun rolloutScore(state: GameState, playerId: Int, cards: List<Card>, random: Random): Double {
        val after = gameController.play(state, playerId, cards)
        if (after.winnerId == playerId) return 10_000.0

        val originalHand = state.player(playerId).hand
        val remaining = after.player(playerId).hand
        val type = state.ruleSet.classify(cards)
        val minOpponentCards = state.players
            .filterNot { it.id == playerId }
            .minOf { it.hand.size }
        val pressure = max(0, 4 - minOpponentCards)

        var score = 1_200.0
        score -= remaining.size * 105.0
        score += cards.size * (18.0 + pressure * 18.0)
        score += (type?.category?.strength ?: 0) * 9.0
        score -= breakPenalty(originalHand, cards) * 24.0
        score -= bombSpendPenalty(state, cards, remaining)
        score += remainingStructureValue(remaining)
        score += simulatedFinishScore(state, remaining, random)
        return score
    }

    private fun simulatedFinishScore(state: GameState, hand: List<Card>, random: Random): Double {
        var simulated = hand.sorted()
        var score = 0.0
        repeat(6) { turn ->
            if (simulated.isEmpty()) {
                score += 700.0 - turn * 55.0
                return@repeat
            }
            val candidates = playableGroups(state, simulated)
            val chosen = candidates
                .sortedWith(
                    compareByDescending<List<Card>> { it.size }
                        .thenByDescending { cards -> state.ruleSet.classify(cards)?.category?.strength ?: 0 }
                        .thenBy { cards -> breakPenalty(simulated, cards) }
                        .thenBy { cards -> cards.maxOrNull() }
                )
                .take(3)
                .randomOrNull(random)
                ?: return@repeat
            simulated = simulated - chosen.toSet()
            score += chosen.size * 38.0
            score -= turn * 18.0
        }
        if (simulated.isEmpty()) score += 500.0 else score -= simulated.size * 34.0
        return score
    }

    private fun playableGroups(state: GameState, hand: List<Card>): List<List<Card>> {
        return listOf(1, 2, 3, 5)
            .flatMap { size -> hand.combinations(size) }
            .filter { state.ruleSet.classify(it) != null }
    }

    private fun remainingStructureValue(hand: List<Card>): Double {
        val groups = hand.groupBy { it.rank }.values
        val pairBonus = groups.count { it.size == 2 } * 22.0
        val tripleBonus = groups.count { it.size == 3 } * 45.0
        val bombBonus = groups.count { it.size >= 4 } * 90.0
        val lowCardBonus = hand.count { it.rank.power <= 4 } * 5.0
        val highCardBonus = hand.count { it.rank.power >= 10 } * 9.0
        return pairBonus + tripleBonus + bombBonus + lowCardBonus + highCardBonus
    }

    private fun bombSpendPenalty(state: GameState, cards: List<Card>, remaining: List<Card>): Double {
        val category = state.ruleSet.classify(cards)?.category ?: return 0.0
        val isBomb = category == HandCategory.FourWithOne || category == HandCategory.StraightFlush
        if (!isBomb || remaining.isEmpty()) return 0.0
        val respondingToStrongHand = state.lastPlayedHand?.type?.category?.strength?.let { it >= HandCategory.FullHouse.strength } == true
        return if (respondingToStrongHand) 180.0 else 760.0
    }

    private fun breakPenalty(hand: List<Card>, cards: List<Card>): Int {
        val rankGroups = hand.groupBy { it.rank }
        return cards.sumOf { card ->
            val groupSize = rankGroups[card.rank]?.size ?: 1
            if (groupSize >= 2) groupSize else 0
        }
    }
}
