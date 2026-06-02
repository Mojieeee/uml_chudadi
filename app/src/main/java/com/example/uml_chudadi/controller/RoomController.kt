package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.NorthRuleSet
import com.example.uml_chudadi.model.RuleSet
import com.example.uml_chudadi.transport.GameMessage
import com.example.uml_chudadi.transport.GameMessageCodec
import com.example.uml_chudadi.transport.GameTransport
import com.example.uml_chudadi.transport.TransportRole
import com.example.uml_chudadi.transport.canStartRoom

data class RoomState(
    val title: String = "本地房间",
    val players: List<String> = listOf("我"),
    val log: List<String> = listOf("房间已创建"),
    val ready: Boolean = false
)

class RoomController(
    private val transport: GameTransport,
    private val ruleSet: RuleSet = NorthRuleSet
) {
    var state: RoomState = RoomState()
        private set

    fun start(role: TransportRole, onChanged: (RoomState) -> Unit) {
        transport.observe { raw ->
            val message = GameMessageCodec.decode(raw)
            state = when (message) {
                is GameMessage.Hello -> state.copy(
                    players = (state.players + message.playerName).distinct(),
                    log = state.log + "${message.playerName} 加入房间"
                )
                is GameMessage.Room -> state.copy(
                    players = message.seats.filter { it.occupied }.map { it.name },
                    log = state.log + "房间人数 ${message.seats.count { it.occupied }}/4",
                    ready = message.seats.canStartRoom()
                )
                is GameMessage.Error -> state.copy(log = state.log + "错误：${message.reason}")
                else -> state.copy(log = state.log + raw)
            }
            onChanged(state)
        }
        transport.start(role)
        transport.send(GameMessageCodec.encode(GameMessage.Hello("玩家", ruleSet.name)))
    }

    fun close() = transport.close()
}
