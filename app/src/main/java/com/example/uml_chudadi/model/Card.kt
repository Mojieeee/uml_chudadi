package com.example.uml_chudadi.model

enum class Suit(val label: String, val power: Int) {
    Diamonds("♦", 0),
    Clubs("♣", 1),
    Hearts("♥", 2),
    Spades("♠", 3)
}

enum class Rank(val label: String, val power: Int) {
    Three("3", 0),
    Four("4", 1),
    Five("5", 2),
    Six("6", 3),
    Seven("7", 4),
    Eight("8", 5),
    Nine("9", 6),
    Ten("10", 7),
    Jack("J", 8),
    Queen("Q", 9),
    King("K", 10),
    Ace("A", 11),
    Two("2", 12)
}

data class Card(val suit: Suit, val rank: Rank) : Comparable<Card> {
    val code: String = "${suit.name.first()}${rank.name}"

    override fun compareTo(other: Card): Int {
        val rankCompare = rank.power.compareTo(other.rank.power)
        return if (rankCompare != 0) rankCompare else suit.power.compareTo(other.suit.power)
    }

    override fun toString(): String = "${suit.label}${rank.label}"

    companion object {
        fun fromCode(code: String): Card? {
            val suit = Suit.entries.firstOrNull { it.name.first().toString() == code.take(1) } ?: return null
            val rankName = code.drop(1)
            val rank = Rank.entries.firstOrNull { it.name == rankName } ?: return null
            return Card(suit, rank)
        }
    }
}

object Deck {
    fun standard(): List<Card> = Suit.entries.flatMap { suit ->
        Rank.entries.map { rank -> Card(suit, rank) }
    }

    fun deal(players: Int = 4, seed: Int? = null): List<List<Card>> {
        val deck = if (seed == null) {
            standard().shuffled()
        } else {
            standard().shuffled(kotlin.random.Random(seed))
        }
        return deck.chunked(deck.size / players).map { it.sorted() }
    }
}
