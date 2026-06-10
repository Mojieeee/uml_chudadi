package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedBluetoothHubTest {
    @Test
    fun threeClientsCanJoinHostWithDistinctSeats() {
        val hub = SimulatedBluetoothHub()
        val host = hub.host()
        val clients = listOf(hub.client(), hub.client(), hub.client())
        val clientRooms = mutableListOf<GameMessage.Room>()
        var seats = defaultRoomSeats("房主")

        host.observe { raw ->
            when (val message = GameMessageCodec.decode(raw)) {
                is GameMessage.Hello -> {
                    val uniqueName = seats.uniqueRoomPlayerName(message.playerName)
                    seats = requireNotNull(seats.addHumanToFirstEmpty(uniqueName))
                    host.send(GameMessageCodec.encode(GameMessage.Room(seats, "北方规则")))
                }
                else -> Unit
            }
        }
        clients.forEach { client ->
            client.observe { raw ->
                val message = GameMessageCodec.decode(raw)
                if (message is GameMessage.Room) clientRooms += message
            }
        }

        clients.forEach { it.start(TransportRole.Client("同名牌友")) }

        assertTrue(seats.canStartRoom().not())
        assertEquals(listOf("同名牌友", "同名牌友2", "同名牌友3"), seats.drop(1).map { it.name })
        assertEquals(9, clientRooms.size)
        assertEquals(seats, clientRooms.last().seats)
    }

    @Test
    fun startMessageAndMoveMessagesRouteThroughHostAuthority() {
        val hub = SimulatedBluetoothHub()
        val host = hub.host()
        val client = hub.client()
        val hostInbox = mutableListOf<GameMessage>()
        val clientInbox = mutableListOf<GameMessage>()
        val seats = requireNotNull(
            requireNotNull(
                requireNotNull(defaultRoomSeats("房主").addHumanToFirstEmpty("好友"))
                    .addAiToFirstEmpty(Difficulty.Easy)
            ).addAiToFirstEmpty(Difficulty.Hard)
        ).setReady(1, true)

        host.observe { raw -> hostInbox += GameMessageCodec.decode(raw) }
        client.observe { raw -> clientInbox += GameMessageCodec.decode(raw) }

        host.send(GameMessageCodec.encode(GameMessage.Start(42, "北方规则", seats)))
        client.send(GameMessageCodec.encode(GameMessage.MoveRequest(1, emptyList(), pass = true)))
        host.send(GameMessageCodec.encode(GameMessage.MoveAccepted(1, 1, emptyList(), pass = true)))

        assertTrue(clientInbox.any { it is GameMessage.Start && it.seed == 42 && it.seats == seats })
        assertTrue(clientInbox.any { it is GameMessage.Start && it.seats.any { seat -> seat.difficulty == Difficulty.Hard } })
        assertTrue(hostInbox.any { it is GameMessage.MoveRequest && it.playerId == 1 && it.pass })
        assertTrue(clientInbox.any { it is GameMessage.MoveAccepted && it.sequence == 1 })
    }

    @Test
    fun fullRoomRejectsFourthClientWithoutChangingExistingSeats() {
        val hub = SimulatedBluetoothHub()
        val host = hub.host()
        val overflowClient = hub.client()
        var seats = defaultRoomSeats("房主")
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友1"))
        seats = requireNotNull(seats.addHumanToFirstEmpty("好友2"))
        seats = requireNotNull(seats.addAiToFirstEmpty(Difficulty.Easy))
        val before = seats
        val overflowInbox = mutableListOf<GameMessage>()

        host.observe { raw ->
            val message = GameMessageCodec.decode(raw)
            if (message is GameMessage.Hello) {
                val updated = seats.addHumanToFirstEmpty(seats.uniqueRoomPlayerName(message.playerName))
                if (updated == null) {
                    host.send(GameMessageCodec.encode(GameMessage.Error("房间已满")))
                } else {
                    seats = updated
                }
            }
        }
        overflowClient.observe { raw -> overflowInbox += GameMessageCodec.decode(raw) }

        overflowClient.start(TransportRole.Client("迟到玩家"))

        assertEquals(before, seats)
        assertTrue(overflowInbox.any { it is GameMessage.Error && it.reason.contains("房间已满") })
    }
}

private class SimulatedBluetoothHub {
    private lateinit var hostEndpoint: Endpoint
    private val clients = mutableListOf<Endpoint>()

    fun host(): GameTransport {
        hostEndpoint = Endpoint(isHost = true)
        return hostEndpoint
    }

    fun client(): GameTransport {
        val endpoint = Endpoint(isHost = false)
        clients += endpoint
        return endpoint
    }

    private inner class Endpoint(private val isHost: Boolean) : GameTransport {
        private var listener: ((TransportEvent) -> Unit)? = null
        private var closed = false

        override fun start(role: TransportRole) {
            if (!isHost && role is TransportRole.Client) {
                send(GameMessageCodec.encode(GameMessage.Hello(role.playerName, "北方规则")))
            }
        }

        override fun send(message: String) {
            if (closed) return
            if (isHost) {
                clients.filterNot { it.closed }.forEach { it.listener?.invoke(TransportEvent.Message(message, "host")) }
            } else {
                if (::hostEndpoint.isInitialized && !hostEndpoint.closed) {
                    hostEndpoint.listener?.invoke(TransportEvent.Message(message, "client-${hashCode()}"))
                }
            }
        }

        override fun observeEvents(listener: (TransportEvent) -> Unit) {
            this.listener = listener
        }

        override fun close() {
            closed = true
        }
    }
}
