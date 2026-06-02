package com.example.uml_chudadi.view

import com.example.uml_chudadi.controller.GameController
import com.example.uml_chudadi.model.NorthRuleSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TableAnimationKeysTest {
    @Test
    fun passDoesNotChangePlayedHandAnimationKey() {
        val controller = GameController(NorthRuleSet)
        val afterPlay = stateAfterFirstLegalPlay(controller)
        val afterPass = controller.pass(afterPlay, afterPlay.currentPlayerId)

        assertEquals(playedHandAnimationKey(afterPlay), playedHandAnimationKey(afterPass))
        assertNotEquals(turnTimerKey(afterPlay), turnTimerKey(afterPass))
    }

    @Test
    fun newPlayedHandChangesPlayedHandAnimationKey() {
        val controller = GameController(NorthRuleSet)
        val afterPlay = stateAfterFirstLegalPlay(controller)
        val legalReply = controller.legalPlays(afterPlay, afterPlay.currentPlayerId).first()
        val afterReply = controller.play(afterPlay, afterPlay.currentPlayerId, legalReply)

        assertNotEquals(playedHandAnimationKey(afterPlay), playedHandAnimationKey(afterReply))
    }

    private fun stateAfterFirstLegalPlay(controller: GameController) =
        (0..200)
            .asSequence()
            .map { seed -> controller.newGame(seed = seed) }
            .mapNotNull { state ->
                val firstCardPlay = controller.legalPlays(state, state.currentPlayerId)
                    .firstOrNull { it == listOf(NorthRuleSet.firstCard) }
                    ?: return@mapNotNull null
                controller.play(state, state.currentPlayerId, firstCardPlay)
            }
            .first { state ->
                state.lastPlayedHand?.type?.cards == listOf(NorthRuleSet.firstCard) &&
                    controller.legalPlays(state, state.currentPlayerId).isNotEmpty()
            }
}
