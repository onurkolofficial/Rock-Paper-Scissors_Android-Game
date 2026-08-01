package com.onurkolofficial.spsgame.model

enum class Move {
    ROCK, PAPER, SCISSORS, IRON, ICE, STEEL;

    fun toId(): String = this.name.lowercase()

    fun getNameRes(): Int {
        return when (this) {
            ROCK -> com.onurkolofficial.spsgame.R.string.game_rock
            PAPER -> com.onurkolofficial.spsgame.R.string.game_paper
            SCISSORS -> com.onurkolofficial.spsgame.R.string.game_scissors
            IRON -> com.onurkolofficial.spsgame.R.string.game_iron
            ICE -> com.onurkolofficial.spsgame.R.string.game_ice
            STEEL -> com.onurkolofficial.spsgame.R.string.game_steel
        }
    }

    companion object {
        fun fromId(id: String?): Move? {
            return when (id?.lowercase()) {
                "rock" -> ROCK
                "paper" -> PAPER
                "scissors" -> SCISSORS
                "iron" -> IRON
                "ice" -> ICE
                "steel" -> STEEL
                else -> null
            }
        }
    }
}
