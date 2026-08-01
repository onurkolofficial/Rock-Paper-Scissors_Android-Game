package com.onurkolofficial.spsgame.model

enum class GameResult {
    WIN, LOSE, DRAW;

    fun toId(): String = this.name.lowercase()

    companion object {
        fun fromId(id: String?): GameResult? {
            return when (id?.lowercase()) {
                "win" -> WIN
                "lose" -> LOSE
                "draw" -> DRAW
                else -> null
            }
        }
    }
}
