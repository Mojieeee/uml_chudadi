package com.example.uml_chudadi.transport

sealed class TransportRole {
    data class Host(val playerName: String) : TransportRole()
    data class Client(val playerName: String, val address: String? = null) : TransportRole()
    data object Local : TransportRole()
}

interface GameTransport {
    fun start(role: TransportRole)
    fun send(message: String)
    fun observe(listener: (String) -> Unit)
    fun close()
}
