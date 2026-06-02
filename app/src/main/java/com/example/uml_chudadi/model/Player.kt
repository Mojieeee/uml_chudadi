package com.example.uml_chudadi.model

enum class PlayerKind {
    Human,
    LocalAi,
    Remote
}

data class Player(
    val id: Int,
    val name: String,
    val kind: PlayerKind = PlayerKind.LocalAi,
    val hand: List<Card> = emptyList(),
    val connected: Boolean = true
) {
    fun remove(cards: List<Card>): Player = copy(hand = hand - cards.toSet())

    fun add(cards: List<Card>): Player = copy(hand = (hand + cards).sorted())
}
