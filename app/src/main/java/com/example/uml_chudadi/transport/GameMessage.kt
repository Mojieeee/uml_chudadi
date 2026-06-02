package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Card
import java.net.URLDecoder
import java.net.URLEncoder

sealed class GameMessage {
    data class Hello(val playerName: String, val ruleName: String) : GameMessage()
    data class Room(val seats: List<RoomSeat>, val ruleName: String) : GameMessage()
    data class Start(val seed: Int, val ruleName: String, val seats: List<RoomSeat>) : GameMessage()
    data class RoomReady(val playerId: Int, val ready: Boolean) : GameMessage()
    data class MoveRequest(val playerId: Int, val cards: List<Card>, val pass: Boolean) : GameMessage()
    data class MoveAccepted(val sequence: Int, val playerId: Int, val cards: List<Card>, val pass: Boolean) : GameMessage()
    data class StateSnapshot(val snapshot: GameSnapshot) : GameMessage()
    data class SyncRequest(val playerId: Int) : GameMessage()
    data class Leave(val playerId: Int) : GameMessage()
    data class Kick(val playerId: Int, val reason: String) : GameMessage()
    data class State(val summary: String) : GameMessage()
    data class Play(val playerId: Int, val cards: List<Card>) : GameMessage()
    data class Pass(val playerId: Int) : GameMessage()
    data class Result(val winnerId: Int) : GameMessage()
    data class Error(val reason: String) : GameMessage()
}

object GameMessageCodec {
    fun encode(message: GameMessage): String {
        return when (message) {
            is GameMessage.Hello -> "HELLO|${pack(message.playerName)}|${pack(message.ruleName)}"
            is GameMessage.Room -> "ROOM|${packSeats(message.seats)}|${pack(message.ruleName)}"
            is GameMessage.Start -> "START|${message.seed}|${pack(message.ruleName)}|${packSeats(message.seats)}"
            is GameMessage.RoomReady -> "ROOM_READY|${message.playerId}|${message.ready}"
            is GameMessage.MoveRequest -> "MOVE_REQUEST|${message.playerId}|${message.pass}|${message.cards.joinToString(",") { it.code }}"
            is GameMessage.MoveAccepted -> "MOVE_ACCEPTED|${message.sequence}|${message.playerId}|${message.pass}|${message.cards.joinToString(",") { it.code }}"
            is GameMessage.StateSnapshot -> "STATE_SNAPSHOT|${packSnapshot(message.snapshot)}"
            is GameMessage.SyncRequest -> "SYNC_REQUEST|${message.playerId}"
            is GameMessage.Leave -> "LEAVE|${message.playerId}"
            is GameMessage.Kick -> "KICK|${message.playerId}|${pack(message.reason)}"
            is GameMessage.State -> "STATE|${pack(message.summary)}"
            is GameMessage.Play -> "PLAY|${message.playerId}|${message.cards.joinToString(",") { it.code }}"
            is GameMessage.Pass -> "PASS|${message.playerId}"
            is GameMessage.Result -> "RESULT|${message.winnerId}"
            is GameMessage.Error -> "ERROR|${pack(message.reason)}"
        }
    }

    fun decode(raw: String): GameMessage {
        val parts = raw.trim().split("|")
        return try {
            when (parts.firstOrNull()) {
                "HELLO" -> GameMessage.Hello(unpack(parts.getOrElse(1) { "玩家" }), unpack(parts.getOrElse(2) { "默认规则" }))
                "ROOM" -> GameMessage.Room(
                    seats = unpackSeats(parts.getOrElse(1) { "" }),
                    ruleName = unpack(parts.getOrElse(2) { "默认规则" })
                )
                "START" -> GameMessage.Start(
                    seed = parts.getOrElse(1) { "0" }.toInt(),
                    ruleName = unpack(parts.getOrElse(2) { "默认规则" }),
                    seats = unpackSeats(parts.getOrElse(3) { "" })
                )
                "ROOM_READY" -> GameMessage.RoomReady(
                    playerId = parts.getOrElse(1) { "0" }.toInt(),
                    ready = parts.getOrElse(2) { "false" }.toBoolean()
                )
                "MOVE_REQUEST" -> GameMessage.MoveRequest(
                    playerId = parts.getOrElse(1) { "0" }.toInt(),
                    pass = parts.getOrElse(2) { "false" }.toBoolean(),
                    cards = parts.getOrElse(3) { "" }.split(",").mapNotNull { Card.fromCode(it) }
                )
                "MOVE_ACCEPTED" -> GameMessage.MoveAccepted(
                    sequence = parts.getOrElse(1) { "0" }.toInt(),
                    playerId = parts.getOrElse(2) { "0" }.toInt(),
                    pass = parts.getOrElse(3) { "false" }.toBoolean(),
                    cards = parts.getOrElse(4) { "" }.split(",").mapNotNull { Card.fromCode(it) }
                )
                "STATE_SNAPSHOT" -> GameMessage.StateSnapshot(unpackSnapshot(parts.getOrElse(1) { "" }))
                "SYNC_REQUEST" -> GameMessage.SyncRequest(parts.getOrElse(1) { "0" }.toInt())
                "LEAVE" -> GameMessage.Leave(parts.getOrElse(1) { "0" }.toInt())
                "KICK" -> GameMessage.Kick(
                    playerId = parts.getOrElse(1) { "0" }.toInt(),
                    reason = unpack(parts.getOrElse(2) { "已离开房间" })
                )
                "STATE" -> GameMessage.State(unpack(parts.getOrElse(1) { "" }))
                "PLAY" -> GameMessage.Play(
                    playerId = parts.getOrElse(1) { "0" }.toInt(),
                    cards = parts.getOrElse(2) { "" }.split(",").mapNotNull { Card.fromCode(it) }
                )
                "PASS" -> GameMessage.Pass(parts.getOrElse(1) { "0" }.toInt())
                "RESULT" -> GameMessage.Result(parts.getOrElse(1) { "0" }.toInt())
                "ERROR" -> GameMessage.Error(unpack(parts.getOrElse(1) { "未知错误" }))
                else -> GameMessage.Error("无法识别消息：$raw")
            }
        } catch (error: RuntimeException) {
            GameMessage.Error("消息解析失败：${error.message ?: raw}")
        }
    }

    private fun pack(value: String): String = URLEncoder.encode(value, "UTF-8")

    private fun unpack(value: String): String = URLDecoder.decode(value, "UTF-8")

    private fun packSeats(seats: List<RoomSeat>): String {
        return seats.normalizedSeats().joinToString(",") { seat ->
            listOf(
                seat.index.toString(),
                seat.kind.name,
                pack(seat.name),
                seat.difficulty?.name.orEmpty(),
                seat.ready.toString(),
                seat.connected.toString()
            ).joinToString(":")
        }
    }

    private fun unpackSeats(value: String): List<RoomSeat> {
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { rawSeat ->
            val parts = rawSeat.split(":", limit = 6)
            val index = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val kind = parts.getOrNull(1)?.let { runCatching { RoomSeatKind.valueOf(it) }.getOrNull() }
                ?: RoomSeatKind.Empty
            val name = unpack(parts.getOrElse(2) { "" })
            val difficulty = parts.getOrNull(3)
                ?.takeIf { it.isNotBlank() }
                ?.let { runCatching { com.example.uml_chudadi.model.Difficulty.valueOf(it) }.getOrNull() }
            val ready = parts.getOrNull(4)?.toBoolean() ?: false
            val connected = parts.getOrNull(5)?.toBoolean() ?: (kind != RoomSeatKind.Empty)
            RoomSeat(index, name, kind, difficulty, ready, connected)
        }.normalizedSeats()
    }

    private fun packSnapshot(snapshot: GameSnapshot): String {
        val players = snapshot.players.joinToString(";") { player ->
            listOf(
                player.id.toString(),
                pack(player.name),
                player.hand.joinToString(",") { it.code }
            ).joinToString("~")
        }
        return listOf(
            snapshot.sequence.toString(),
            snapshot.seed.toString(),
            pack(snapshot.ruleProfileId),
            pack(players),
            snapshot.currentPlayerId.toString(),
            snapshot.lastPlayerId?.toString().orEmpty(),
            snapshot.lastCards.joinToString(",") { it.code },
            snapshot.passCount.toString(),
            snapshot.firstTurn.toString(),
            snapshot.winnerId?.toString().orEmpty(),
            pack(snapshot.message)
        ).joinToString("#")
    }

    private fun unpackSnapshot(value: String): GameSnapshot {
        val parts = value.split("#", limit = 11)
        val players = unpack(parts.getOrElse(3) { "" })
            .split(";")
            .filter { it.isNotBlank() }
            .mapNotNull { rawPlayer ->
                val playerParts = rawPlayer.split("~", limit = 3)
                val id = playerParts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val name = unpack(playerParts.getOrElse(1) { "" })
                val hand = playerParts.getOrElse(2) { "" }.split(",").mapNotNull { Card.fromCode(it) }
                SnapshotPlayer(id, name, hand)
            }
        return GameSnapshot(
            sequence = parts.getOrElse(0) { "0" }.toInt(),
            seed = parts.getOrElse(1) { "0" }.toInt(),
            ruleProfileId = unpack(parts.getOrElse(2) { "north" }),
            players = players,
            currentPlayerId = parts.getOrElse(4) { "0" }.toInt(),
            lastPlayerId = parts.getOrNull(5)?.takeIf { it.isNotBlank() }?.toIntOrNull(),
            lastCards = parts.getOrElse(6) { "" }.split(",").mapNotNull { Card.fromCode(it) },
            passCount = parts.getOrElse(7) { "0" }.toInt(),
            firstTurn = parts.getOrElse(8) { "true" }.toBoolean(),
            winnerId = parts.getOrNull(9)?.takeIf { it.isNotBlank() }?.toIntOrNull(),
            message = unpack(parts.getOrElse(10) { "" })
        )
    }
}
