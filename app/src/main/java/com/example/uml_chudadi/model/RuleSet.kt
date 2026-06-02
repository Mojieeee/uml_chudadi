package com.example.uml_chudadi.model

enum class BombMode {
    None,
    Enhanced
}

data class RuleProfile(
    val id: String,
    val title: String,
    val description: String,
    val firstCard: Card,
    val bombMode: BombMode,
    val fiveCardRanking: Boolean,
    val sameSizeOnly: Boolean
)

interface RuleSet {
    val profile: RuleProfile
    val name: String
        get() = profile.title
    val description: String
        get() = profile.description
    val firstCard: Card
        get() = profile.firstCard
    val bombEnhanced: Boolean
        get() = profile.bombMode == BombMode.Enhanced

    fun classify(cards: List<Card>): HandType? = HandClassifier.classify(cards)

    fun canLead(cards: List<Card>, isFirstTurn: Boolean): Boolean {
        val type = classify(cards) ?: return false
        return if (isFirstTurn) firstCard in type.cards else true
    }

    fun canBeat(previous: HandType, next: HandType): Boolean {
        if (previous.category == next.category) {
            val rankCompare = next.primaryRank.power.compareTo(previous.primaryRank.power)
            return if (rankCompare != 0) rankCompare > 0 else next.highCard > previous.highCard
        }

        if (profile.sameSizeOnly && previous.cards.size != next.cards.size) {
            return false
        }

        if (bombEnhanced && next.isBombPattern() && !previous.isBombPattern()) {
            return true
        }

        return profile.fiveCardRanking &&
            previous.isFiveCardPattern() &&
            next.isFiveCardPattern() &&
            next.category.strength > previous.category.strength
    }
}

object NorthRuleSet : RuleSet {
    override val profile: RuleProfile = RuleProfile(
        id = "north",
        title = "北方规则",
        description = "黑桃3首出，四带一和同花顺可作为强牌压制普通牌型。",
        firstCard = Card(Suit.Spades, Rank.Three),
        bombMode = BombMode.Enhanced,
        fiveCardRanking = true,
        sameSizeOnly = false
    )
}

object SouthRuleSet : RuleSet {
    override val profile: RuleProfile = RuleProfile(
        id = "south",
        title = "南方规则",
        description = "方块3首出，经典同张数压制，五张牌之间按等级比较。",
        firstCard = Card(Suit.Diamonds, Rank.Three),
        bombMode = BombMode.None,
        fiveCardRanking = true,
        sameSizeOnly = true
    )
}

val AvailableRuleSets: List<RuleSet> = listOf(NorthRuleSet, SouthRuleSet)

fun ruleSetByIdOrName(value: String): RuleSet {
    return AvailableRuleSets.firstOrNull { ruleSet ->
        value == ruleSet.profile.id || value.contains(ruleSet.name) || value.contains(ruleSet.profile.title)
    } ?: when {
        value.contains("南") -> SouthRuleSet
        else -> NorthRuleSet
    }
}

private fun HandType.isFiveCardPattern(): Boolean {
    return cards.size == 5 && category.strength >= HandCategory.Straight.strength
}

private fun HandType.isBombPattern(): Boolean {
    return category == HandCategory.FourWithOne || category == HandCategory.StraightFlush
}
