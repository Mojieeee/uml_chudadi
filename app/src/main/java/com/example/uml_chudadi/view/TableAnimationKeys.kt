package com.example.uml_chudadi.view

import com.example.uml_chudadi.model.GameState

fun playedHandAnimationKey(state: GameState): String {
    val last = state.lastPlayedHand
    return if (last == null) {
        "none-${state.currentPlayerId}-${state.firstTurn}"
    } else {
        "${last.playerId}-${last.type.cards.joinToString { it.code }}"
    }
}

fun turnTimerKey(state: GameState): String {
    return "${state.currentPlayerId}-${playedHandAnimationKey(state)}-${state.passCount}-${state.firstTurn}"
}
