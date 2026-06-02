package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.PlayerKind

object NetworkMoveGuard {
    fun canHostAcceptMove(state: GameState, request: GameMessage.MoveRequest): Boolean {
        val player = state.players.firstOrNull { it.id == request.playerId } ?: return false
        if (state.isFinished) return false
        if (state.currentPlayerId != request.playerId) return false
        return player.kind == PlayerKind.Remote
    }

    fun wasMoveApplied(
        previous: GameState,
        next: GameState,
        playerId: Int,
        cards: List<com.example.uml_chudadi.model.Card>,
        pass: Boolean
    ): Boolean {
        if (previous == next) return false
        if (previous.currentPlayerId != playerId) return false
        return if (pass) {
            wasPassApplied(previous, next, playerId)
        } else {
            val last = next.lastPlayedHand ?: return false
            last.playerId == playerId &&
                last.type.cards.toSet() == cards.toSet() &&
                next.player(playerId).hand.size == previous.player(playerId).hand.size - cards.size
        }
    }

    private fun wasPassApplied(previous: GameState, next: GameState, playerId: Int): Boolean {
        val previousHand = previous.lastPlayedHand ?: return false
        val normalPass = next.passCount == previous.passCount + 1 &&
            next.currentPlayerId == previous.nextPlayerId(playerId) &&
            next.lastPlayedHand == previous.lastPlayedHand
        val roundReset = previous.passCount + 1 >= previous.players.size - 1 &&
            next.passCount == 0 &&
            next.lastPlayedHand == null &&
            next.currentPlayerId == previousHand.playerId
        return normalPass || roundReset
    }
}
