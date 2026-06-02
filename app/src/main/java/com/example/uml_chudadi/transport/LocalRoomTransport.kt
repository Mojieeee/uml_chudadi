package com.example.uml_chudadi.transport

class LocalRoomTransport : GameTransport {
    private var listener: ((String) -> Unit)? = null
    private var seats = defaultRoomSeats("你")

    override fun start(role: TransportRole) {
        listener?.invoke(GameMessageCodec.encode(GameMessage.Room(seats, "北方规则")))
        listener?.invoke(GameMessageCodec.encode(GameMessage.State("牌局已准备，随时可以开局")))
    }

    override fun send(message: String) {
        val decoded = GameMessageCodec.decode(message)
        if (decoded is GameMessage.Hello && seats.none { it.name == decoded.playerName }) {
            seats = seats.addHumanToFirstEmpty(decoded.playerName) ?: seats
            listener?.invoke(GameMessageCodec.encode(GameMessage.Room(seats, decoded.ruleName)))
        } else {
            listener?.invoke(message)
        }
    }

    override fun observe(listener: (String) -> Unit) {
        this.listener = listener
    }

    override fun close() {
        listener = null
    }
}
