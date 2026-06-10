package com.example.uml_chudadi.transport

class LocalRoomTransport : GameTransport {
    private var listener: ((TransportEvent) -> Unit)? = null
    private var seats = defaultRoomSeats("你")

    override fun start(role: TransportRole) {
        emit(GameMessageCodec.encode(GameMessage.Room(seats, "北方规则")))
        emit(GameMessageCodec.encode(GameMessage.State("牌局已准备，随时可以开局")))
    }

    override fun send(message: String) {
        val decoded = GameMessageCodec.decode(message)
        if (decoded is GameMessage.Hello && seats.none { it.name == decoded.playerName }) {
            seats = seats.addHumanToFirstEmpty(decoded.playerName) ?: seats
            emit(GameMessageCodec.encode(GameMessage.Room(seats, decoded.ruleName)))
        } else {
            emit(message)
        }
    }

    override fun observeEvents(listener: (TransportEvent) -> Unit) {
        this.listener = listener
    }

    override fun close() {
        listener = null
    }

    private fun emit(raw: String) {
        listener?.invoke(TransportEvent.Message(raw))
    }
}
