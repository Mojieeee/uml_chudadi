package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomSeatTest {
    @Test
    fun emptySeatsPreventStartUntilRoomIsFull() {
        var seats = defaultRoomSeats("房主")

        assertFalse(seats.canStartRoom())
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Easy))
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Hard))
        assertFalse(seats.canStartRoom())
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友"))
        seats = seats.setReady(seats.first { it.name == "好友" }.index, true)

        assertTrue(seats.canStartRoom())
    }

    @Test
    fun aiSeatsCanToggleDifficultyAndBeRemoved() {
        var seats = requireNotNull(defaultRoomSeats("房主").addAiToFirstEmpty(Difficulty.Easy))
        val aiIndex = seats.first { it.kind == RoomSeatKind.Ai }.index

        seats = seats.toggleAiDifficulty(aiIndex)
        assertEquals(Difficulty.Normal, seats.first { it.index == aiIndex }.difficulty)

        seats = seats.removeAi(aiIndex)
        val removedSeat = seats.first { it.index == aiIndex }
        assertEquals(RoomSeatKind.Empty, removedSeat.kind)
        assertNull(removedSeat.difficulty)
    }

    @Test
    fun fullRoomRejectsExtraHuman() {
        var seats = defaultRoomSeats("房主")
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友1"))
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友2"))
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Easy))

        assertNull(seats.addHumanToFirstEmpty("好友3"))
    }

    @Test
    fun repeatedPlayerNamesAreMadeUniqueBeforeJoining() {
        var seats = defaultRoomSeats("房主")
        seats = requireNotNull(seats.addHumanToFirstEmpty("牌友"))
        seats = requireNotNull(seats.addHumanToFirstEmpty(seats.uniqueRoomPlayerName("牌友")))

        val names = seats.filter { it.kind == RoomSeatKind.Human }.map { it.name }

        assertEquals(listOf("牌友", "牌友2"), names)
        assertEquals("牌友3", seats.uniqueRoomPlayerName("牌友"))
    }

    @Test
    fun rematchResetKeepsSeatsButClearsHumanReady() {
        var seats = defaultRoomSeats("房主")
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友"))
        val friendIndex = seats.first { it.name == "好友" }.index
        seats = seats.setReady(friendIndex, true)
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Hard))
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Easy))

        val reset = seats.resetForRematch()

        assertEquals(RoomSeatKind.Host, reset[0].kind)
        assertTrue(reset[0].ready)
        assertEquals("好友", reset[friendIndex].name)
        assertFalse(reset[friendIndex].ready)
        assertTrue(reset.filter { it.kind == RoomSeatKind.Ai }.all { it.ready && it.connected })
    }

    @Test
    fun disconnectedSeatPreventsStartAndClearsReady() {
        var seats = defaultRoomSeats("房主")
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友"))
        val friendIndex = seats.first { it.name == "好友" }.index
        seats = seats.setReady(friendIndex, true)
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Easy))
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Normal))

        assertTrue(seats.canStartRoom())

        seats = seats.setConnected(friendIndex, false)

        assertFalse(seats.canStartRoom())
        assertFalse(seats.first { it.index == friendIndex }.ready)
    }
}
