package com.onurkolofficial.spsgame.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GameEngineTest {

    @Test
    fun standardRules_rockBeatsScissors() {
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ROCK, Move.SCISSORS))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.SCISSORS, Move.ROCK))
    }

    @Test
    fun standardRules_paperBeatsRock() {
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.PAPER, Move.ROCK))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ROCK, Move.PAPER))
    }

    @Test
    fun standardRules_scissorsBeatsPaper() {
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.SCISSORS, Move.PAPER))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.PAPER, Move.SCISSORS))
    }

    @Test
    fun standardRules_draws() {
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.ROCK, Move.ROCK))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.PAPER, Move.PAPER))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.SCISSORS, Move.SCISSORS))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.IRON, Move.IRON))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.ICE, Move.ICE))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.STEEL, Move.STEEL))
    }

    @Test
    fun specialPower_iceRules() {
        // Ice beats rock, paper, scissors, iron. Loses to steel.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.PAPER))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.IRON))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ICE, Move.STEEL))

        // Opposite perspective
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.ICE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ROCK, Move.ICE))
    }

    @Test
    fun specialPower_steelRules() {
        // Steel beats iron, rock, scissors, ice. Loses to paper.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.IRON))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.ICE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.STEEL, Move.PAPER))

        // Opposite perspective
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.PAPER, Move.STEEL))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.STEEL))
    }

    @Test
    fun specialPower_ironRules() {
        // Iron beats rock, scissors. Loses to paper.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.IRON, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.IRON, Move.SCISSORS))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.PAPER))

        // Opposite perspective
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.PAPER, Move.IRON))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ROCK, Move.IRON))
    }
}
