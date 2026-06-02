package com.example.uml_chudadi.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HandClassifierTest {
    @Test
    fun classifyCommonHands() {
        assertEquals(HandCategory.Single, HandClassifier.classify(cards("DThree"))?.category)
        assertEquals(HandCategory.Pair, HandClassifier.classify(cards("DThree", "SThree"))?.category)
        assertEquals(HandCategory.Triple, HandClassifier.classify(cards("DThree", "CThree", "SThree"))?.category)
        assertEquals(HandCategory.Straight, HandClassifier.classify(cards("DThree", "CFour", "HFive", "SSix", "DSeven"))?.category)
        assertEquals(HandCategory.Flush, HandClassifier.classify(cards("DThree", "DFive", "DSeven", "DNine", "DJack"))?.category)
        assertEquals(HandCategory.FullHouse, HandClassifier.classify(cards("DThree", "CThree", "HThree", "SAce", "DAce"))?.category)
        assertEquals(HandCategory.FourWithOne, HandClassifier.classify(cards("DThree", "CThree", "HThree", "SThree", "DAce"))?.category)
        assertEquals(HandCategory.StraightFlush, HandClassifier.classify(cards("DThree", "DFour", "DFive", "DSix", "DSeven"))?.category)
    }

    @Test
    fun twoCannotBeUsedInStraight() {
        assertNull(HandClassifier.classify(cards("DTen", "CJack", "HQueen", "SKing", "DTwo")))
    }

    @Test
    fun fiveCardHandsCanBeatByCategoryInBothRules() {
        val single = requireNotNull(HandClassifier.classify(cards("DFour")))
        val pair = requireNotNull(HandClassifier.classify(cards("DFour", "CFour")))
        val triple = requireNotNull(HandClassifier.classify(cards("DFour", "CFour", "HFour")))
        val straight = requireNotNull(HandClassifier.classify(cards("DThree", "CFour", "HFive", "SSix", "DSeven")))
        val flush = requireNotNull(HandClassifier.classify(cards("DThree", "DFive", "DSeven", "DNine", "DJack")))
        val fullHouse = requireNotNull(HandClassifier.classify(cards("DThree", "CThree", "HThree", "SAce", "DAce")))
        val fourWithOne = requireNotNull(HandClassifier.classify(cards("DFour", "CFour", "HFour", "SFour", "DAce")))
        val straightFlush = requireNotNull(HandClassifier.classify(cards("DThree", "DFour", "DFive", "DSix", "DSeven")))

        assertTrue(NorthRuleSet.canLead(cards("SThree"), isFirstTurn = true))
        assertTrue(SouthRuleSet.canLead(cards("DThree"), isFirstTurn = true))
        assertTrue(NorthRuleSet.canBeat(flush, fullHouse))
        assertTrue(NorthRuleSet.canBeat(flush, fourWithOne))
        assertTrue(NorthRuleSet.canBeat(fourWithOne, straightFlush))
        assertTrue(NorthRuleSet.canBeat(single, fourWithOne))
        assertTrue(NorthRuleSet.canBeat(pair, straightFlush))
        assertTrue(NorthRuleSet.canBeat(triple, fourWithOne))
        assertFalse(NorthRuleSet.canBeat(single, flush))
        assertFalse(NorthRuleSet.canBeat(straightFlush, fourWithOne))
        assertTrue(SouthRuleSet.canBeat(straight, flush))
        assertTrue(SouthRuleSet.canBeat(flush, fullHouse))
        assertTrue(SouthRuleSet.canBeat(flush, fourWithOne))
        assertTrue(SouthRuleSet.canBeat(fourWithOne, straightFlush))
        assertFalse(SouthRuleSet.canBeat(flush, straight))
        assertFalse(SouthRuleSet.canBeat(single, fourWithOne))
        assertFalse(SouthRuleSet.canBeat(pair, straightFlush))
        assertFalse(SouthRuleSet.canBeat(triple, fourWithOne))
        assertFalse(SouthRuleSet.canBeat(single, straightFlush))
        assertFalse(SouthRuleSet.canBeat(straightFlush, single))
    }

    @Test
    fun ruleProfilesKeepExpectedBombModes() {
        val single = requireNotNull(HandClassifier.classify(cards("DFour")))
        val fourWithOne = requireNotNull(HandClassifier.classify(cards("DFour", "CFour", "HFour", "SFour", "DAce")))

        assertTrue(NorthRuleSet.canBeat(single, fourWithOne))
        assertFalse(SouthRuleSet.canBeat(single, fourWithOne))
        assertTrue(SouthRuleSet.profile.fiveCardRanking)
        assertTrue(SouthRuleSet.profile.sameSizeOnly)
        assertFalse(NorthRuleSet.profile.sameSizeOnly)
        assertEquals(NorthRuleSet, ruleSetByIdOrName("north"))
        assertEquals(SouthRuleSet, ruleSetByIdOrName("南方规则"))
    }

    @Test
    fun fullHouseUsesTripleRankForComparison() {
        val lowTriple = requireNotNull(HandClassifier.classify(cards("DThree", "CThree", "HThree", "SAce", "DAce")))
        val highTriple = requireNotNull(HandClassifier.classify(cards("DFour", "CFour", "HFour", "SKing", "DKing")))

        assertTrue(NorthRuleSet.canBeat(lowTriple, highTriple))
    }

    private fun cards(vararg codes: String): List<Card> {
        return codes.map { code ->
            val suit = when (code.first()) {
                'D' -> Suit.Diamonds
                'C' -> Suit.Clubs
                'H' -> Suit.Hearts
                'S' -> Suit.Spades
                else -> error("bad suit")
            }
            val rank = Rank.valueOf(code.drop(1))
            Card(suit, rank)
        }
    }
}
