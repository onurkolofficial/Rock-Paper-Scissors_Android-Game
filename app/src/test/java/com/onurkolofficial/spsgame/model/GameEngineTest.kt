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
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.FIRE, Move.FIRE))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.LIGHTNING, Move.LIGHTNING))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.BOMB, Move.BOMB))
    }

    @Test
    fun specialPower_bombRules() {
        // Bomb beats Rock, Iron, Steel, Scissors, Fire, Lightning. Loses to Paper, Ice.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.IRON))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.STEEL))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.FIRE))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.BOMB, Move.LIGHTNING))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.BOMB, Move.PAPER))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.BOMB, Move.ICE))
    }

    @Test
    fun specialPower_lightningRules() {
        // Lightning beats Steel, Iron, Scissors, Ice, Fire. Loses to Rock, Paper, Bomb.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.LIGHTNING, Move.STEEL))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.LIGHTNING, Move.IRON))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.LIGHTNING, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.LIGHTNING, Move.ICE))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.LIGHTNING, Move.FIRE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.LIGHTNING, Move.ROCK))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.LIGHTNING, Move.PAPER))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.LIGHTNING, Move.BOMB))
    }

    @Test
    fun specialPower_fireRules() {
        // Fire beats Paper, Ice, Scissors. Loses to Rock, Steel, Bomb, Lightning. Draws with Iron.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.FIRE, Move.PAPER))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.FIRE, Move.ICE))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.FIRE, Move.SCISSORS))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.FIRE, Move.ROCK))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.FIRE, Move.STEEL))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.FIRE, Move.BOMB))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.FIRE, Move.LIGHTNING))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.FIRE, Move.IRON))
    }

    @Test
    fun specialPower_iceRules() {
        // Ice beats rock, paper, scissors, iron, bomb. Loses to steel, fire, lightning.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.PAPER))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.IRON))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.ICE, Move.BOMB))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ICE, Move.STEEL))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ICE, Move.FIRE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.ICE, Move.LIGHTNING))
    }

    @Test
    fun specialPower_steelRules() {
        // Steel beats iron, rock, scissors, ice, fire. Loses to paper, lightning, bomb.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.IRON))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.SCISSORS))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.ICE))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.STEEL, Move.FIRE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.STEEL, Move.PAPER))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.STEEL, Move.LIGHTNING))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.STEEL, Move.BOMB))
    }

    @Test
    fun specialPower_ironRules() {
        // Iron beats rock, scissors. Loses to paper, ice, steel, lightning, bomb. Draws with fire.
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.IRON, Move.ROCK))
        assertEquals(GameResult.WIN, GameEngine.determineWinner(Move.IRON, Move.SCISSORS))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.PAPER))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.ICE))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.STEEL))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.LIGHTNING))
        assertEquals(GameResult.LOSE, GameEngine.determineWinner(Move.IRON, Move.BOMB))
        assertEquals(GameResult.DRAW, GameEngine.determineWinner(Move.IRON, Move.FIRE))
    }
}
