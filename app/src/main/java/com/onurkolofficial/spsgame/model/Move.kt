package com.onurkolofficial.spsgame.model

enum class Move {
    ROCK, PAPER, SCISSORS, IRON, ICE, STEEL, FIRE, LIGHTNING, BOMB;

    fun toId(): String = this.name.lowercase()

    fun getNameRes(): Int {
        return when (this) {
            ROCK -> com.onurkolofficial.spsgame.R.string.game_rock
            PAPER -> com.onurkolofficial.spsgame.R.string.game_paper
            SCISSORS -> com.onurkolofficial.spsgame.R.string.game_scissors
            IRON -> com.onurkolofficial.spsgame.R.string.game_iron
            ICE -> com.onurkolofficial.spsgame.R.string.game_ice
            STEEL -> com.onurkolofficial.spsgame.R.string.game_steel
            FIRE -> com.onurkolofficial.spsgame.R.string.game_fire
            LIGHTNING -> com.onurkolofficial.spsgame.R.string.game_lightning
            BOMB -> com.onurkolofficial.spsgame.R.string.game_bomb
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
                "fire" -> FIRE
                "lightning" -> LIGHTNING
                "bomb" -> BOMB
                else -> null
            }
        }
    }
}
