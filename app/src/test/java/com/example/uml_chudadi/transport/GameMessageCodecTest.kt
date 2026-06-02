package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.Difficulty
import com.example.uml_chudadi.model.Rank
import com.example.uml_chudadi.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMessageCodecTest {
    @Test
    fun playMessageRoundTripsCards() {
        val message = GameMessage.Play(
            playerId = 2,
            cards = listOf(Card(Suit.Spades, Rank.Three), Card(Suit.Hearts, Rank.Ace))
        )

        val decoded = GameMessageCodec.decode(GameMessageCodec.encode(message))

        assertEquals(message, decoded)
    }

    @Test
    fun invalidMessageReturnsError() {
        val decoded = GameMessageCodec.decode("NOPE|bad")

        assertTrue(decoded is GameMessage.Error)
    }

    @Test
    fun startMessageRoundTripsRoomPlayersWithSeparators() {
        val seats = listOf(
            RoomSeat(0, "你", RoomSeatKind.Host),
            RoomSeat(1, "好友,A", RoomSeatKind.Human),
            RoomSeat(2, "玩家|B", RoomSeatKind.Human),
            RoomSeat(3, "困难人机", RoomSeatKind.Ai, Difficulty.Hard)
        )
        val message = GameMessage.Start(
            seed = 2026,
            ruleName = "南方规则|快速",
            seats = seats
        )

        val decoded = GameMessageCodec.decode(GameMessageCodec.encode(message))

        assertEquals(message, decoded)
    }

    @Test
    fun roomMessageRoundTripsSeatSnapshot() {
        val message = GameMessage.Room(
            seats = listOf(
                RoomSeat(0, "房主", RoomSeatKind.Host, ready = true, connected = true),
                RoomSeat(1, "", RoomSeatKind.Empty),
                RoomSeat(2, "简单人机", RoomSeatKind.Ai, Difficulty.Easy, ready = true, connected = true),
                RoomSeat(3, "好友|C", RoomSeatKind.Human, ready = false, connected = true)
            ),
            ruleName = "北方规则"
        )

        val decoded = GameMessageCodec.decode(GameMessageCodec.encode(message))

        assertEquals(message, decoded)
    }

    @Test
    fun readyMoveAndSnapshotMessagesRoundTrip() {
        val ready = GameMessage.RoomReady(playerId = 2, ready = true)
        assertEquals(ready, GameMessageCodec.decode(GameMessageCodec.encode(ready)))

        val kick = GameMessage.Kick(playerId = 3, reason = "座位已移出|重新加入")
        assertEquals(kick, GameMessageCodec.decode(GameMessageCodec.encode(kick)))

        val accepted = GameMessage.MoveAccepted(
            sequence = 7,
            playerId = 1,
            pass = false,
            cards = listOf(Card(Suit.Diamonds, Rank.Three))
        )
        assertEquals(accepted, GameMessageCodec.decode(GameMessageCodec.encode(accepted)))

        val snapshot = GameSnapshot(
            sequence = 8,
            seed = 2026,
            ruleProfileId = "north",
            players = listOf(SnapshotPlayer(0, "你|A", listOf(Card(Suit.Spades, Rank.Ace)))),
            currentPlayerId = 0,
            lastPlayerId = null,
            lastCards = emptyList(),
            passCount = 0,
            firstTurn = true,
            winnerId = null,
            message = "同步#状态"
        )
        val message = GameMessage.StateSnapshot(snapshot)
        assertEquals(message, GameMessageCodec.decode(GameMessageCodec.encode(message)))
    }
}
