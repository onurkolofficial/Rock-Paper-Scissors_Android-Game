package com.onurkolofficial.spsgame.model

enum class Move {
    ROCK, PAPER, SCISSORS, IRON, ICE, STEEL;

    fun toId(): String = this.name.lowercase()

    companion object {
        fun fromId(id: String?): Move {
            return when (id?.lowercase()) {
                "rock" -> ROCK
                "paper" -> PAPER
                "scissors" -> SCISSORS
                "iron" -> IRON
                "ice" -> ICE
                "steel" -> STEEL
                else -> ROCK
            }
        }
    }
}
