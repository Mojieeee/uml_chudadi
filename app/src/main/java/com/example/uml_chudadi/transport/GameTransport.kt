package com.example.uml_chudadi.transport

sealed class TransportRole {
    data class Host(val playerName: String, val clientId: String = "") : TransportRole()
    data class Client(val playerName: String, val address: String? = null, val clientId: String = "", val rejoinSeatIndex: Int? = null) : TransportRole()
    data object Local : TransportRole()
}

sealed class TransportEvent {
    data class Message(val raw: String, val peerKey: String? = null) : TransportEvent()
    data class PeerConnected(val peerKey: String, val deviceName: String = "", val deviceAddress: String = "") : TransportEvent()
    data class PeerDisconnected(val peerKey: String, val reason: String = "连接已断开") : TransportEvent()
    data class Error(val reason: String, val peerKey: String? = null) : TransportEvent()
}

interface GameTransport {
    fun start(role: TransportRole)
    fun send(message: String)
    fun sendTo(peerKey: String, message: String) = send(message)
    fun observe(listener: (String) -> Unit) {
        observeEvents { event ->
            when (event) {
                is TransportEvent.Message -> listener(event.raw)
                is TransportEvent.Error -> listener(GameMessageCodec.encode(GameMessage.Error(event.reason)))
                else -> Unit
            }
        }
    }
    fun observeEvents(listener: (TransportEvent) -> Unit)
    fun close()
}
