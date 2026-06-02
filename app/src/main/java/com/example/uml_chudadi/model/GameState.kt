package com.example.uml_chudadi.model

data class GameState(
    val players: List<Player>,
    val ruleSet: RuleSet,
    val currentPlayerId: Int,
    val lastPlayedHand: PlayedHand? = null,
    val passCount: Int = 0,
    val firstTurn: Boolean = true,
    val winnerId: Int? = null,
    val message: String = "准备开始"
) {
    val currentPlayer: Player = players.first { it.id == currentPlayerId }
    val isFinished: Boolean = winnerId != null

    fun nextPlayerId(from: Int = currentPlayerId): Int {
        val index = players.indexOfFirst { it.id == from }
        return players[(index + 1).floorMod(players.size)].id
    }

    fun player(id: Int): Player = players.first { it.id == id }

    fun updatePlayer(player: Player): GameState = copy(
        players = players.map { if (it.id == player.id) player else it }
    )
}

private fun Int.floorMod(mod: Int): Int = ((this % mod) + mod) % mod
