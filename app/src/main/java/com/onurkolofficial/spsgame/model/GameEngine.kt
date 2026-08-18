package com.onurkolofficial.spsgame.model

object GameEngine {
    val MOVES = listOf(Move.ROCK, Move.PAPER, Move.SCISSORS)

    fun determineWinner(playerMove: Move, opponentMove: Move): GameResult {
        if (playerMove == opponentMove) return GameResult.DRAW

        // BOMB rules: Beats ROCK, IRON, STEEL, SCISSORS, FIRE, LIGHTNING. Loses to PAPER, ICE.
        if (playerMove == Move.BOMB) {
            if (opponentMove == Move.ROCK || opponentMove == Move.IRON || opponentMove == Move.STEEL ||
                opponentMove == Move.SCISSORS || opponentMove == Move.FIRE || opponentMove == Move.LIGHTNING) return GameResult.WIN
            if (opponentMove == Move.PAPER || opponentMove == Move.ICE) return GameResult.LOSE
        }
        if (opponentMove == Move.BOMB) {
            if (playerMove == Move.ROCK || playerMove == Move.IRON || playerMove == Move.STEEL ||
                playerMove == Move.SCISSORS || playerMove == Move.FIRE || playerMove == Move.LIGHTNING) return GameResult.LOSE
            if (playerMove == Move.PAPER || playerMove == Move.ICE) return GameResult.WIN
        }

        // LIGHTNING rules: Beats STEEL, IRON, SCISSORS, ICE, FIRE. Loses to ROCK, PAPER.
        if (playerMove == Move.LIGHTNING) {
            if (opponentMove == Move.STEEL || opponentMove == Move.IRON || opponentMove == Move.SCISSORS ||
                opponentMove == Move.ICE || opponentMove == Move.FIRE) return GameResult.WIN
            if (opponentMove == Move.ROCK || opponentMove == Move.PAPER) return GameResult.LOSE
        }
        if (opponentMove == Move.LIGHTNING) {
            if (playerMove == Move.STEEL || playerMove == Move.IRON || playerMove == Move.SCISSORS ||
                playerMove == Move.ICE || playerMove == Move.FIRE) return GameResult.LOSE
            if (playerMove == Move.ROCK || playerMove == Move.PAPER) return GameResult.WIN
        }

        // FIRE rules: Beats PAPER, ICE, SCISSORS. Loses to ROCK, STEEL. Draws with IRON.
        if (playerMove == Move.FIRE) {
            if (opponentMove == Move.PAPER || opponentMove == Move.ICE || opponentMove == Move.SCISSORS) return GameResult.WIN
            if (opponentMove == Move.ROCK || opponentMove == Move.STEEL) return GameResult.LOSE
            if (opponentMove == Move.IRON) return GameResult.DRAW
        }
        if (opponentMove == Move.FIRE) {
            if (playerMove == Move.PAPER || playerMove == Move.ICE || playerMove == Move.SCISSORS) return GameResult.LOSE
            if (playerMove == Move.ROCK || playerMove == Move.STEEL) return GameResult.WIN
            if (playerMove == Move.IRON) return GameResult.DRAW
        }

        // ICE rules: Beats ROCK, PAPER, SCISSORS, IRON. Loses to STEEL.
        if (playerMove == Move.ICE) {
            if (opponentMove == Move.ROCK || opponentMove == Move.PAPER || opponentMove == Move.SCISSORS || opponentMove == Move.IRON) return GameResult.WIN
            if (opponentMove == Move.STEEL) return GameResult.LOSE
        }
        if (opponentMove == Move.ICE) {
            if (playerMove == Move.ROCK || playerMove == Move.PAPER || playerMove == Move.SCISSORS || playerMove == Move.IRON) return GameResult.LOSE
            if (playerMove == Move.STEEL) return GameResult.WIN
        }

        // STEEL rules: Beats IRON, ROCK, SCISSORS. Loses to PAPER.
        if (playerMove == Move.STEEL) {
            if (opponentMove == Move.IRON || opponentMove == Move.ROCK || opponentMove == Move.SCISSORS) return GameResult.WIN
            if (opponentMove == Move.PAPER) return GameResult.LOSE
        }
        if (opponentMove == Move.STEEL) {
            if (playerMove == Move.IRON || playerMove == Move.ROCK || playerMove == Move.SCISSORS) return GameResult.LOSE
            if (playerMove == Move.PAPER) return GameResult.WIN
        }

        // IRON rules: Beats ROCK, SCISSORS. Loses to PAPER.
        if (playerMove == Move.IRON) {
            if (opponentMove == Move.ROCK || opponentMove == Move.SCISSORS) return GameResult.WIN
            if (opponentMove == Move.PAPER) return GameResult.LOSE
        }
        if (opponentMove == Move.IRON) {
            if (playerMove == Move.ROCK || playerMove == Move.SCISSORS) return GameResult.LOSE
            if (playerMove == Move.PAPER) return GameResult.WIN
        }

        // Standard rules
        if ((playerMove == Move.ROCK && opponentMove == Move.SCISSORS) ||
            (playerMove == Move.PAPER && opponentMove == Move.ROCK) ||
            (playerMove == Move.SCISSORS && opponentMove == Move.PAPER)) {
            return GameResult.WIN
        }

        return GameResult.LOSE
    }

    fun getRandomMove(): Move {
        return MOVES.random()
    }
}
