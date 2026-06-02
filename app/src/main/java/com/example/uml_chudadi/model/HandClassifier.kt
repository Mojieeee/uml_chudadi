package com.example.uml_chudadi.model

object HandClassifier {
    fun classify(cards: List<Card>): HandType? {
        val sorted = cards.sorted()
        if (sorted.isEmpty()) return null
        return when (sorted.size) {
            1 -> hand(HandCategory.Single, sorted, sorted.last().rank)
            2 -> classifyPair(sorted)
            3 -> classifyTriple(sorted)
            5 -> classifyFive(sorted)
            else -> null
        }
    }

    private fun classifyPair(cards: List<Card>): HandType? {
        return if (cards.map { it.rank }.distinct().size == 1) {
            hand(HandCategory.Pair, cards, cards.first().rank)
        } else {
            null
        }
    }

    private fun classifyTriple(cards: List<Card>): HandType? {
        return if (cards.map { it.rank }.distinct().size == 1) {
            hand(HandCategory.Triple, cards, cards.first().rank)
        } else {
            null
        }
    }

    private fun classifyFive(cards: List<Card>): HandType? {
        val byRank = cards.groupBy { it.rank }
        val isFlush = cards.map { it.suit }.distinct().size == 1
        val isStraight = isStraight(cards)

        return when {
            isStraight && isFlush -> hand(HandCategory.StraightFlush, cards, cards.maxBy { it.rank.power }.rank)
            byRank.values.any { it.size == 4 } -> {
                val rank = byRank.entries.first { it.value.size == 4 }.key
                hand(HandCategory.FourWithOne, cards, rank)
            }
            byRank.values.map { it.size }.sorted() == listOf(2, 3) -> {
                val rank = byRank.entries.first { it.value.size == 3 }.key
                hand(HandCategory.FullHouse, cards, rank)
            }
            isFlush -> hand(HandCategory.Flush, cards, cards.last().rank)
            isStraight -> hand(HandCategory.Straight, cards, cards.maxBy { it.rank.power }.rank)
            else -> null
        }
    }

    private fun isStraight(cards: List<Card>): Boolean {
        val powers = cards.map { it.rank.power }.distinct().sorted()
        if (powers.size != 5) return false
        if (Rank.Two.power in powers) return false
        return powers.zipWithNext().all { (a, b) -> b == a + 1 }
    }

    private fun hand(category: HandCategory, cards: List<Card>, primaryRank: Rank): HandType {
        return HandType(
            category = category,
            cards = cards.sorted(),
            primaryRank = primaryRank,
            highCard = cards.sorted().last()
        )
    }
}
