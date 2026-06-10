package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Difficulty

enum class RoomSeatKind {
    Empty,
    Host,
    Human,
    Ai
}

enum class SeatConnectionState {
    Empty,
    Online,
    Disconnected,
    Migrating
}

data class RoomSeat(
    val index: Int,
    val name: String,
    val kind: RoomSeatKind,
    val difficulty: Difficulty? = null,
    val ready: Boolean = false,
    val connected: Boolean = false,
    val clientId: String = "",
    val deviceAddress: String = "",
    val connectionState: SeatConnectionState = if (connected) SeatConnectionState.Online else if (kind == RoomSeatKind.Empty) SeatConnectionState.Empty else SeatConnectionState.Disconnected,
    val takeoverByAi: Boolean = false
) {
    val occupied: Boolean = kind != RoomSeatKind.Empty
    val online: Boolean = connected && connectionState == SeatConnectionState.Online
    val controlledByAi: Boolean = kind == RoomSeatKind.Ai || takeoverByAi
}

fun defaultRoomSeats(hostName: String = "你"): List<RoomSeat> {
    return listOf(
        RoomSeat(0, hostName, RoomSeatKind.Host, ready = true, connected = true),
        RoomSeat(1, "", RoomSeatKind.Empty),
        RoomSeat(2, "", RoomSeatKind.Empty),
        RoomSeat(3, "", RoomSeatKind.Empty)
    )
}

fun emptyRoomSeats(): List<RoomSeat> {
    return (0..3).map { index -> RoomSeat(index, "", RoomSeatKind.Empty) }
}

fun List<RoomSeat>.normalizedSeats(): List<RoomSeat> {
    val byIndex = associateBy { it.index }
    return (0..3).map { index ->
        byIndex[index] ?: RoomSeat(index, "", RoomSeatKind.Empty)
    }
}

fun List<RoomSeat>.canStartRoom(): Boolean {
    return normalizedSeats().all { it.occupied && it.ready && it.connected }
}

fun List<RoomSeat>.addHumanToFirstEmpty(name: String): List<RoomSeat>? {
    val seats = normalizedSeats()
    val empty = seats.firstOrNull { it.kind == RoomSeatKind.Empty } ?: return null
    return seats.replaceSeat(empty.index, RoomSeat(empty.index, name, RoomSeatKind.Human, ready = false, connected = true))
}

fun List<RoomSeat>.addOrRejoinHuman(
    name: String,
    clientId: String,
    deviceAddress: String = "",
    preferredIndex: Int? = null
): Pair<List<RoomSeat>, Int>? {
    val seats = normalizedSeats()
    val reconnectSeat = seats.firstOrNull { seat ->
        seat.kind == RoomSeatKind.Human &&
            clientId.isNotBlank() &&
            seat.clientId == clientId &&
            (preferredIndex == null || seat.index == preferredIndex)
    }
    if (reconnectSeat != null) {
        val restored = reconnectSeat.copy(
            name = name.ifBlank { reconnectSeat.name },
            ready = false,
            connected = true,
            clientId = clientId,
            deviceAddress = deviceAddress,
            connectionState = SeatConnectionState.Online,
            takeoverByAi = false
        )
        return seats.replaceSeat(reconnectSeat.index, restored) to reconnectSeat.index
    }
    val empty = seats.firstOrNull { it.kind == RoomSeatKind.Empty } ?: return null
    val seat = RoomSeat(
        index = empty.index,
        name = name,
        kind = RoomSeatKind.Human,
        ready = false,
        connected = true,
        clientId = clientId,
        deviceAddress = deviceAddress,
        connectionState = SeatConnectionState.Online
    )
    return seats.replaceSeat(empty.index, seat) to empty.index
}

fun List<RoomSeat>.uniqueRoomPlayerName(baseName: String): String {
    val base = baseName.trim().ifBlank { "好友" }
    val names = normalizedSeats().map { it.name }.toSet()
    if (base !in names) return base
    var suffix = 2
    while ("$base$suffix" in names) suffix += 1
    return "$base$suffix"
}

fun List<RoomSeat>.addAiToFirstEmpty(difficulty: Difficulty): List<RoomSeat>? {
    val seats = normalizedSeats()
    val empty = seats.firstOrNull { it.kind == RoomSeatKind.Empty } ?: return null
    val aiCount = seats.count { it.kind == RoomSeatKind.Ai } + 1
    return seats.replaceSeat(empty.index, RoomSeat(empty.index, "人机$aiCount", RoomSeatKind.Ai, difficulty, ready = true, connected = true))
}

fun List<RoomSeat>.removeAi(index: Int): List<RoomSeat> {
    return normalizedSeats().replaceSeat(index, RoomSeat(index, "", RoomSeatKind.Empty))
}

fun List<RoomSeat>.toggleAiDifficulty(index: Int): List<RoomSeat> {
    val seats = normalizedSeats()
    val seat = seats.firstOrNull { it.index == index } ?: return seats
    if (seat.kind != RoomSeatKind.Ai) return seats
    val nextDifficulty = when (seat.difficulty) {
        Difficulty.Easy -> Difficulty.Normal
        Difficulty.Normal -> Difficulty.Hard
        Difficulty.Hard, null -> Difficulty.Easy
    }
    return seats.replaceSeat(index, seat.copy(difficulty = nextDifficulty))
}

fun List<RoomSeat>.setReady(index: Int, ready: Boolean): List<RoomSeat> {
    val seats = normalizedSeats()
    val seat = seats.firstOrNull { it.index == index } ?: return seats
    if (!seat.occupied) return seats
    return seats.replaceSeat(index, seat.copy(ready = ready))
}

fun List<RoomSeat>.setConnected(index: Int, connected: Boolean): List<RoomSeat> {
    val seats = normalizedSeats()
    val seat = seats.firstOrNull { it.index == index } ?: return seats
    if (!seat.occupied) return seats
    val ready = if (connected) seat.ready else false
    val state = if (connected) SeatConnectionState.Online else SeatConnectionState.Disconnected
    return seats.replaceSeat(index, seat.copy(connected = connected, ready = ready, connectionState = state))
}

fun List<RoomSeat>.markHumanDisconnected(index: Int, takeoverByAi: Boolean = true): List<RoomSeat> {
    val seats = normalizedSeats()
    val seat = seats.firstOrNull { it.index == index } ?: return seats
    if (seat.kind != RoomSeatKind.Human && seat.kind != RoomSeatKind.Host) return seats
    return seats.replaceSeat(
        index,
        seat.copy(
            ready = false,
            connected = false,
            connectionState = SeatConnectionState.Disconnected,
            takeoverByAi = takeoverByAi
        )
    )
}

fun List<RoomSeat>.markMigrating(): List<RoomSeat> {
    return normalizedSeats().map { seat ->
        if (seat.kind == RoomSeatKind.Human && seat.connected) {
            seat.copy(connectionState = SeatConnectionState.Migrating)
        } else {
            seat
        }
    }.normalizedSeats()
}

fun List<RoomSeat>.promoteHost(newHostIndex: Int, oldHostIndex: Int = 0): List<RoomSeat> {
    val seats = normalizedSeats()
    return seats.map { seat ->
        when (seat.index) {
            oldHostIndex -> seat.copy(
                kind = RoomSeatKind.Human,
                ready = false,
                connected = false,
                connectionState = SeatConnectionState.Disconnected,
                takeoverByAi = true
            )
            newHostIndex -> seat.copy(
                kind = RoomSeatKind.Host,
                ready = true,
                connected = true,
                connectionState = SeatConnectionState.Online,
                takeoverByAi = false
            )
            else -> if (seat.connectionState == SeatConnectionState.Migrating && seat.connected) {
                seat.copy(connectionState = SeatConnectionState.Online)
            } else {
                seat
            }
        }
    }.normalizedSeats()
}

fun List<RoomSeat>.firstMigrationCandidate(localPlayerId: Int? = null): RoomSeat? {
    return normalizedSeats()
        .filter { it.kind == RoomSeatKind.Human && it.connected && !it.takeoverByAi }
        .minByOrNull { it.index }
}

fun List<RoomSeat>.resetForRematch(): List<RoomSeat> {
    return normalizedSeats().map { seat ->
        when (seat.kind) {
            RoomSeatKind.Empty -> RoomSeat(seat.index, "", RoomSeatKind.Empty)
            RoomSeatKind.Host -> seat.copy(ready = true, connected = true, connectionState = SeatConnectionState.Online, takeoverByAi = false)
            RoomSeatKind.Human -> seat.copy(ready = false, connectionState = if (seat.connected) SeatConnectionState.Online else SeatConnectionState.Disconnected)
            RoomSeatKind.Ai -> seat.copy(ready = true, connected = true, connectionState = SeatConnectionState.Online, takeoverByAi = false)
        }
    }.normalizedSeats()
}

private fun List<RoomSeat>.replaceSeat(index: Int, seat: RoomSeat): List<RoomSeat> {
    return map { if (it.index == index) seat else it }.normalizedSeats()
}
