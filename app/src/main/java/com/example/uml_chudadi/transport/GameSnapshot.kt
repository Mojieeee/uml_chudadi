package com.example.uml_chudadi.transport

import com.example.uml_chudadi.model.Card

data class SnapshotPlayer(
    val id: Int,
    val name: String,
    val hand: List<Card>
)

data class GameSnapshot(
    val sequence: Int,
    val seed: Int,
    val ruleProfileId: String,
    val players: List<SnapshotPlayer>,
    val currentPlayerId: Int,
    val lastPlayerId: Int?,
    val lastCards: List<Card>,
    val passCount: Int,
    val firstTurn: Boolean,
    val winnerId: Int?,
    val message: String,
    val roomId: String = "",
    val hostEpoch: Int = 0
)
