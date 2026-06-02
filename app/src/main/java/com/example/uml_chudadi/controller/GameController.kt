package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.Deck
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.Move
import com.example.uml_chudadi.model.PlayedHand
import com.example.uml_chudadi.model.Player
import com.example.uml_chudadi.model.PlayerKind
import com.example.uml_chudadi.model.RuleSet

class GameController(private val ruleSet: RuleSet) {
    fun newGame(
        humanName: String = "我",
        aiNames: List<String> = listOf("小北", "阿豪", "星河"),
        seed: Int? = null
    ): GameState {
        val hands = Deck.deal(seed = seed)
        val players = listOf(
            Player(0, humanName, PlayerKind.Human, hands[0]),
            Player(1, aiNames.getOrElse(0) { "小北" }, PlayerKind.LocalAi, hands[1]),
            Player(2, aiNames.getOrElse(1) { "阿豪" }, PlayerKind.LocalAi, hands[2]),
            Player(3, aiNames.getOrElse(2) { "星河" }, PlayerKind.LocalAi, hands[3])
        )
        val starter = players.first { ruleSet.firstCard in it.hand }
        return GameState(
            players = players,
            ruleSet = ruleSet,
            currentPlayerId = starter.id,
            message = "${starter.name} 持有 ${ruleSet.firstCard}，先手出牌"
        )
    }

    fun play(state: GameState, playerId: Int, cards: List<Card>): GameState {
        if (state.isFinished) return state
        if (state.currentPlayerId != playerId) return state.copy(message = "还没轮到该玩家")
        if (!state.player(playerId).hand.containsAll(cards)) return state.copy(message = "选择的牌不在手牌中")

        val hand = state.ruleSet.classify(cards) ?: return state.copy(message = "牌型不合法")
        if (state.lastPlayedHand == null) {
            if (!state.ruleSet.canLead(cards, state.firstTurn)) {
                return state.copy(message = "首出必须包含 ${state.ruleSet.firstCard}")
            }
        } else if (!state.ruleSet.canBeat(state.lastPlayedHand.type, hand)) {
            return state.copy(message = "需要压过上一手 ${state.lastPlayedHand.type.label}")
        }

        val updatedPlayer = state.player(playerId).remove(cards)
        val winner = if (updatedPlayer.hand.isEmpty()) playerId else null
        return state.updatePlayer(updatedPlayer).copy(
            currentPlayerId = state.nextPlayerId(playerId),
            lastPlayedHand = PlayedHand(playerId, hand),
            passCount = 0,
            firstTurn = false,
            winnerId = winner,
            message = if (winner == null) {
                "${updatedPlayer.name} 打出 ${hand.label}: ${cards.sorted().joinToString(" ")}"
            } else {
                "${updatedPlayer.name} 获胜"
            }
        )
    }

    fun pass(state: GameState, playerId: Int): GameState {
        if (state.isFinished) return state
        if (state.currentPlayerId != playerId) return state.copy(message = "还没轮到该玩家")
        val previous = state.lastPlayedHand ?: return state.copy(message = "首家不能过牌")
        val nextPassCount = state.passCount + 1
        if (nextPassCount >= state.players.size - 1) {
            return state.copy(
                currentPlayerId = previous.playerId,
                lastPlayedHand = null,
                passCount = 0,
                firstTurn = false,
                message = "其他玩家均过牌，${state.player(previous.playerId).name} 获得新一轮先手"
            )
        }
        return state.copy(
            currentPlayerId = state.nextPlayerId(playerId),
            passCount = nextPassCount,
            message = "${state.player(playerId).name} 过牌"
        )
    }

    fun applyMove(state: GameState, move: Move): GameState {
        return when (move) {
            is Move.Play -> play(state, move.playerId, move.cards)
            is Move.Pass -> pass(state, move.playerId)
        }
    }

    fun legalPlays(state: GameState, playerId: Int): List<List<Card>> {
        val hand = state.player(playerId).hand
        val sizes = listOf(1, 2, 3, 5)
        return sizes.flatMap { size -> hand.combinations(size) }
            .filter { cards ->
                val type = state.ruleSet.classify(cards) ?: return@filter false
                if (state.lastPlayedHand == null) {
                    state.ruleSet.canLead(cards, state.firstTurn)
                } else {
                    state.ruleSet.canBeat(state.lastPlayedHand.type, type)
                }
            }
            .sortedWith(compareBy<List<Card>> { it.size }.thenBy { it.maxOrNull() })
    }
}

fun <T> List<T>.combinations(size: Int): List<List<T>> {
    if (size == 0) return listOf(emptyList())
    if (size > this.size) return emptyList()
    if (size == 1) return map { listOf(it) }
    val result = mutableListOf<List<T>>()
    for (index in 0..this.size - size) {
        val head = this[index]
        val tails = subList(index + 1, this.size).combinations(size - 1)
        tails.forEach { result += listOf(head) + it }
    }
    return result
}
