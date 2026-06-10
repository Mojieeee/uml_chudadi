package com.example.uml_chudadi.controller

import com.example.uml_chudadi.model.Card
import com.example.uml_chudadi.model.GameState
import com.example.uml_chudadi.model.HandCategory
import com.example.uml_chudadi.model.HandType
import com.example.uml_chudadi.model.Move
import kotlin.math.max
import kotlin.math.tanh

enum class NeuralAiProfile(val title: String, val scoreNoise: Double) {
    Easy("简单", 3.0),
    Normal("普通", 0.80),
    Hard("困难", 0.0)
}

class NeuralAiStrategy(
    private val gameController: GameController,
    private val profile: NeuralAiProfile = NeuralAiProfile.Normal
) : AiStrategy {
    override val name: String = profile.title

    override fun chooseMove(state: GameState, playerId: Int): Move {
        val legal = gameController.legalPlays(state, playerId)
        if (legal.isEmpty()) return Move.Pass(playerId)

        val hand = state.player(playerId).hand
        legal.firstOrNull { it.size == hand.size }?.let { return Move.Play(playerId, it) }

        val best = legal
            .map { cards ->
                val type = state.ruleSet.classify(cards)
                val baseScore = NeuralMoveModel.score(NeuralMoveFeatures.encode(state, playerId, cards))
                ScoredNeuralPlay(
                    cards = cards,
                    score = baseScore + profile.scoreNoise * deterministicNoise(state, playerId, cards),
                    categoryStrength = type?.category?.strength ?: 0,
                    highCard = cards.maxOrNull()
                )
            }
            .maxWith(
                compareBy<ScoredNeuralPlay> { it.score }
                    .thenBy { it.cards.size }
                    .thenBy { it.categoryStrength }
                    .thenByDescending { it.highCard }
            )
        return Move.Play(playerId, best.cards)
    }

    private fun deterministicNoise(state: GameState, playerId: Int, cards: List<Card>): Double {
        if (profile.scoreNoise == 0.0) return 0.0
        var hash = 17
        hash = hash * 31 + playerId
        hash = hash * 31 + state.currentPlayerId
        hash = hash * 31 + state.passCount
        hash = hash * 31 + if (state.firstTurn) 1 else 0
        hash = hash * 31 + (state.lastPlayedHand?.type?.category?.strength ?: 0)
        state.players.forEach { player ->
            hash = hash * 31 + player.hand.size
        }
        cards.sorted().forEach { card ->
            hash = hash * 31 + card.rank.power * 4 + card.suit.power
        }
        val mixed = hash xor (hash ushr 16)
        return ((mixed and 0xFFFF) / 32767.5) - 1.0
    }

    private data class ScoredNeuralPlay(
        val cards: List<Card>,
        val score: Double,
        val categoryStrength: Int,
        val highCard: Card?
    )
}

object NeuralMoveFeatures {
    const val FEATURE_COUNT = 23

    fun encode(state: GameState, playerId: Int, cards: List<Card>): DoubleArray {
        val player = state.player(playerId)
        val hand = player.hand
        val type = state.ruleSet.classify(cards)
        val remaining = hand - cards.toSet()
        val opponentSizes = state.players.filterNot { it.id == playerId }.map { it.hand.size }
        val minOpponentCards = opponentSizes.minOrNull() ?: 13
        val maxOpponentCards = opponentSizes.maxOrNull() ?: 13
        val actionSize = cards.size.coerceAtLeast(1)
        val highRank = cards.maxOrNull()?.rank?.power ?: 0
        val highSuit = cards.maxOrNull()?.suit?.power ?: 0
        val categoryStrength = type?.category?.strength ?: 0
        val breaks = breakPenalty(hand, cards)
        val respondingToStrongHand = state.lastPlayedHand?.type?.category?.strength?.let {
            it >= HandCategory.FullHouse.strength
        } == true

        return doubleArrayOf(
            actionSize / 5.0,
            categoryStrength / HandCategory.StraightFlush.strength.toDouble(),
            (type?.primaryRank?.power ?: highRank) / 12.0,
            highRank / 12.0,
            highSuit / 3.0,
            remaining.size / 13.0,
            hand.size / 13.0,
            minOpponentCards / 13.0,
            maxOpponentCards / 13.0,
            max(0, 4 - minOpponentCards) / 4.0,
            state.passCount / 3.0,
            if (state.firstTurn) 1.0 else 0.0,
            if (state.lastPlayedHand != null) 1.0 else 0.0,
            if (type.isBomb()) 1.0 else 0.0,
            if (type?.cards?.size == 5) 1.0 else 0.0,
            breaks / 8.0,
            cards.count { it.rank.power <= 4 } / actionSize.toDouble(),
            cards.count { it.rank.power >= 10 } / actionSize.toDouble(),
            groupPotential(remaining),
            runPotential(remaining),
            if (state.ruleSet.bombEnhanced) 1.0 else 0.0,
            if (remaining.isEmpty()) 1.0 else 0.0,
            if (respondingToStrongHand) 1.0 else 0.0
        )
    }

    private fun HandType?.isBomb(): Boolean {
        return this?.category == HandCategory.FourWithOne || this?.category == HandCategory.StraightFlush
    }

    private fun breakPenalty(hand: List<Card>, cards: List<Card>): Int {
        val rankGroups = hand.groupBy { it.rank }
        return cards.sumOf { card ->
            val groupSize = rankGroups[card.rank]?.size ?: 1
            if (groupSize >= 2) groupSize else 0
        }
    }

    private fun groupPotential(cards: List<Card>): Double {
        if (cards.isEmpty()) return 1.0
        val score = cards.groupBy { it.rank }.values.sumOf { group ->
            when (group.size) {
                1 -> 0.0
                2 -> 1.0
                3 -> 1.8
                else -> 2.5
            }
        }
        return (score / 8.0).coerceIn(0.0, 1.0)
    }

    private fun runPotential(cards: List<Card>): Double {
        if (cards.size < 5) return 0.0
        val ranks = cards.map { it.rank.power }.distinct().sorted()
        var best = 1
        var current = 1
        for (index in 1 until ranks.size) {
            current = if (ranks[index] == ranks[index - 1] + 1) current + 1 else 1
            best = max(best, current)
        }
        return (best / 5.0).coerceIn(0.0, 1.0)
    }
}

object NeuralMoveModel {
    const val VERSION = "mlp-selfplay-distilled-v3"
    const val WEIGHT_CHECKSUM = "1445351bc0209981dc29104a4178dc0b49baf6fac47ab02a827b8675fd3726e6"
    const val TRAINING_SEED = 20260610
    const val SELF_PLAY_GAMES = 32
    const val HIDDEN_UNITS = 16

    val inputSize: Int
        get() = NeuralMoveFeatures.FEATURE_COUNT

    val hiddenLayerSize: Int
        get() = hiddenWeights.size

    val parameterCount: Int
        get() = hiddenWeights.sumOf { it.size } + hiddenBias.size + outputWeights.size + 1

    private val hiddenWeights = arrayOf(
        doubleArrayOf(0.088168, 0.044293, -0.058752, -0.014807, 0.021767, -0.057067, -0.030279, -0.034604, 0.031235, 0.063299, 0.046966, -0.015884, -0.017397, -0.014964, 0.042062, -0.103085, 0.012410, -0.027253, 0.000110, 0.054258, 0.015160, 0.127127, 0.048927),
        doubleArrayOf(0.359543, 0.180831, -0.166885, -0.164593, 0.032021, -0.353381, -0.228511, 0.050093, 0.102438, 0.098801, -0.014929, -0.026968, 0.125282, -0.395814, 0.193649, -0.201191, -0.019934, -0.040744, 0.097517, 0.072406, 0.058240, 0.261170, 0.363635),
        doubleArrayOf(-0.784951, -0.660563, 0.117812, -0.044676, 0.046897, 0.635159, 0.365507, -0.173395, 0.003748, 0.113951, -0.010531, 0.355326, 0.175306, -0.880291, -0.102734, 0.197402, -0.101801, -0.022515, -0.304836, -0.200329, 0.024361, -0.048754, 1.019703),
        doubleArrayOf(-0.169974, 0.050576, 0.223962, 0.071847, 0.028583, 0.273291, 0.241323, 0.138969, -0.030876, -0.665716, -0.014254, -0.066533, -0.340179, 0.908621, -0.020077, 0.308312, -0.360044, 0.235460, -0.124847, -0.094168, -0.082354, -0.516005, -0.632321),
        doubleArrayOf(-0.361072, -0.180845, 0.020837, 0.087388, 0.023617, 0.314646, 0.141635, -0.021078, -0.045215, -0.125702, -0.030022, 0.111153, 0.086736, 0.204230, -0.187789, 0.302467, -0.022256, 0.080978, -0.074886, -0.165307, -0.179067, -0.326761, -0.206519),
        doubleArrayOf(0.305517, 0.142342, -0.034158, -0.056958, -0.035308, -0.297128, -0.176810, -0.004029, -0.005965, 0.202725, -0.037896, -0.104749, -0.104457, -0.160809, 0.110755, -0.308022, -0.017145, -0.113314, 0.091696, 0.170131, -0.017224, 0.277920, 0.075845),
        doubleArrayOf(0.360165, 0.337344, 0.279355, -0.256635, 0.129581, -0.725049, -0.594245, 0.255726, -0.238345, 0.273341, -0.073672, -0.068174, 0.010903, -0.312927, 0.297620, -0.088218, 0.023825, -0.045952, -0.330681, -0.034799, 0.204411, 1.354393, -0.219727),
        doubleArrayOf(0.518095, 0.217126, -0.003601, -0.136959, 0.043619, -0.085752, 0.075417, -0.064533, -0.012589, 0.476444, 0.147416, 0.010021, -0.073147, -0.192984, 0.058830, -0.192367, 0.209618, -0.020926, 0.374836, 0.226213, -0.185357, 1.257924, 0.203920),
        doubleArrayOf(-0.245090, -0.170221, 0.065333, 0.148222, 0.004141, 0.232953, 0.100978, -0.076185, -0.060143, -0.144139, -0.050155, 0.060917, -0.003113, 0.178477, -0.143080, 0.265311, -0.030619, 0.062162, -0.031784, -0.086090, -0.093159, -0.263539, -0.145059),
        doubleArrayOf(0.200129, 0.127295, -0.010684, -0.075047, 0.020953, -0.168047, -0.095159, 0.034242, 0.027354, 0.138042, -0.017401, -0.069100, 0.009716, -0.128825, 0.103860, -0.235835, 0.061577, -0.080803, 0.049028, 0.088976, -0.003843, 0.246935, 0.117627),
        doubleArrayOf(-0.144944, -0.116391, 0.014268, 0.048046, -0.036439, 0.093516, 0.055531, 0.021703, 0.006537, -0.143120, -0.053808, 0.052608, 0.047971, 0.076037, -0.036215, 0.174387, -0.028337, 0.070586, -0.054915, -0.098267, 0.015893, -0.152610, -0.064702),
        doubleArrayOf(-0.128370, -0.128566, -0.211142, -0.279421, -0.074105, 0.296046, 0.258346, 0.088021, 0.036440, -0.864566, -0.070304, 0.173017, 0.040932, 0.216185, 0.241903, 0.537302, -0.087158, 0.410572, -0.068046, -0.003815, 0.116333, -0.376155, 0.032337),
        doubleArrayOf(0.161922, -0.356686, -0.328494, 0.268418, -0.046480, 0.385944, 0.523356, 0.408124, 0.331899, -0.038807, 0.254300, 0.175710, -0.025176, -1.132406, 0.085040, 0.281507, 0.258431, 0.052880, -0.893494, 1.102535, -0.231148, -1.510971, -0.003020),
        doubleArrayOf(-0.242208, -0.229637, -0.133512, 0.112826, 0.275304, 0.583721, 0.508244, 0.055029, -0.007854, -0.675172, 0.318549, -0.011244, -0.294812, 0.027926, -0.063016, 0.218381, -0.009703, 0.340204, 0.199167, -0.082749, -0.258944, -0.586656, -0.312468),
        doubleArrayOf(-0.220956, -0.142090, 0.011823, 0.100076, -0.039146, 0.149454, 0.084713, -0.007230, -0.031037, -0.151123, -0.018163, 0.107093, -0.003211, 0.137611, -0.031251, 0.219564, -0.044575, 0.064144, -0.031617, -0.097555, 0.027381, -0.236857, -0.053513),
        doubleArrayOf(0.453792, 0.276261, -0.154642, -0.124345, 0.117977, -0.687168, -0.573712, 0.111118, 0.062585, 0.212130, 0.447295, 0.356378, -0.293908, -0.248490, -0.105193, 0.012975, 0.174236, 0.382095, 0.505822, -0.086805, -0.303270, 0.054945, 0.183098)
    )

    private val hiddenBias = doubleArrayOf(-0.003762, 0.007140, -0.344292, -0.809217, 0.095041, 0.283474, -0.769288, -1.566919, 0.092479, -0.035358, -0.040866, -0.546983, 1.967777, 0.122790, -0.002747, 1.273402)

    private val outputWeights = doubleArrayOf(0.068712, 0.230303, -0.542763, -0.599122, -0.234302, 0.206291, 0.792196, 1.153097, -0.182218, 0.149206, -0.113408, -0.424232, -1.432537, -0.421799, -0.144565, 0.467243)
    private const val OUTPUT_BIAS = 2.069912

    fun score(features: DoubleArray): Double {
        require(features.size == NeuralMoveFeatures.FEATURE_COUNT) {
            "Expected ${NeuralMoveFeatures.FEATURE_COUNT} features, got ${features.size}"
        }
        var output = OUTPUT_BIAS
        for (unit in hiddenWeights.indices) {
            val activation = tanh(dot(hiddenWeights[unit], features) + hiddenBias[unit])
            output += activation * outputWeights[unit]
        }
        return output
    }

    private fun dot(weights: DoubleArray, features: DoubleArray): Double {
        var sum = 0.0
        for (index in weights.indices) {
            sum += weights[index] * features[index]
        }
        return sum
    }
}
