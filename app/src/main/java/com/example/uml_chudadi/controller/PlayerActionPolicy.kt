package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.PlayerKind

data class LocalActionAvailability(
    val canAct: Boolean,
    val canPass: Boolean,
    val hasLegalPlay: Boolean,
    val notice: String?
)

object PlayerActionPolicy {
    fun evaluate(
        state: GameState,
        localPlayerId: Int,
        gameStarted: Boolean = true,
        inputLocked: Boolean = false,
        isPlayAnimating: Boolean = false
    ): LocalActionAvailability {
        val localPlayer = state.player(localPlayerId)
        val isLocalTurn = gameStarted && state.currentPlayerId == localPlayerId && localPlayer.kind == PlayerKind.Human
        val legalPlays = if (isLocalTurn) {
            GameController(state.ruleSet).legalPlays(state, localPlayerId)
        } else {
            emptyList()
        }
        val canAct = isLocalTurn && !inputLocked && !isPlayAnimating
        val notice = when {
            !isLocalTurn -> null
            inputLocked -> "等待房主确认..."
            state.lastPlayedHand != null && legalPlays.isEmpty() -> "手上没有可以大过人家的牌"
            else -> null
        }
        return LocalActionAvailability(
            canAct = canAct,
            canPass = canAct && state.lastPlayedHand != null,
            hasLegalPlay = legalPlays.isNotEmpty(),
            notice = notice
        )
    }
}
