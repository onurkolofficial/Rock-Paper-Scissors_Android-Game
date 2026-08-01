package com.onurkolofficial.spsgame.model

object GameEngine {
    val MOVES = listOf(Move.ROCK, Move.PAPER, Move.SCISSORS)

    fun determineWinner(playerMove: Move, opponentMove: Move): GameResult {
        if (playerMove == opponentMove) return GameResult.DRAW

        // Ice rules: Beats rock, paper, scissors, iron. Loses to steel.
        if (playerMove == Move.ICE) {
            if (opponentMove == Move.ROCK || opponentMove == Move.PAPER || opponentMove == Move.SCISSORS || opponentMove == Move.IRON) return GameResult.WIN
            if (opponentMove == Move.STEEL) return GameResult.LOSE
        }
        if (opponentMove == Move.ICE) {
            if (playerMove == Move.ROCK || playerMove == Move.PAPER || playerMove == Move.SCISSORS || playerMove == Move.IRON) return GameResult.LOSE
            if (playerMove == Move.STEEL) return GameResult.WIN
        }

        // Steel rules: Beats iron, rock, scissors, ice. Loses to paper.
        if (playerMove == Move.STEEL) {
            if (opponentMove == Move.IRON || opponentMove == Move.ROCK || opponentMove == Move.SCISSORS || opponentMove == Move.ICE) return GameResult.WIN
            if (opponentMove == Move.PAPER) return GameResult.LOSE
        }
        if (opponentMove == Move.STEEL) {
            if (playerMove == Move.IRON || playerMove == Move.ROCK || playerMove == Move.SCISSORS || playerMove == Move.ICE) return GameResult.LOSE
            if (playerMove == Move.PAPER) return GameResult.WIN
        }

        // Iron rules: Beats rock, scissors. Loses to paper.
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
