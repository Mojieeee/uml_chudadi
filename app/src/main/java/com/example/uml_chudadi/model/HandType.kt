package com.example.uml_chudadi.model

enum class HandCategory(val label: String, val strength: Int) {
    Single("单张", 1),
    Pair("对子", 2),
    Triple("三张", 3),
    Straight("顺子", 4),
    Flush("同花", 5),
    FullHouse("葫芦", 6),
    FourWithOne("四带一", 7),
    StraightFlush("同花顺", 8)
}

data class HandType(
    val category: HandCategory,
    val cards: List<Card>,
    val primaryRank: Rank,
    val highCard: Card
) {
    val label: String = category.label
}

sealed class Move {
    data class Play(val playerId: Int, val cards: List<Card>) : Move()
    data class Pass(val playerId: Int) : Move()
}

data class PlayedHand(
    val playerId: Int,
    val type: HandType
)
