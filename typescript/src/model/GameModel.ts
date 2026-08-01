export type Move = 'rock' | 'paper' | 'scissors' | 'iron' | 'ice' | 'steel';
export type Result = 'win' | 'lose' | 'draw' | null;

export const MOVES: Move[] = ['rock', 'paper', 'scissors'];

export const determineWinner = (playerMove: Move, opponentMove: Move): Result => {
  if (playerMove === opponentMove) return 'draw';
  
  // Ice rules: Beats rock, paper, scissors, iron. Let's say loses to steel.
  if (playerMove === 'ice') {
    if (opponentMove === 'rock' || opponentMove === 'paper' || opponentMove === 'scissors' || opponentMove === 'iron') return 'win';
    if (opponentMove === 'steel') return 'lose';
  }
  if (opponentMove === 'ice') {
    if (playerMove === 'rock' || playerMove === 'paper' || playerMove === 'scissors' || playerMove === 'iron') return 'lose';
    if (playerMove === 'steel') return 'win';
  }

  // Steel rules: Beats iron, rock, scissors, ice(from above). Loses to paper.
  if (playerMove === 'steel') {
    if (opponentMove === 'iron' || opponentMove === 'rock' || opponentMove === 'scissors' || opponentMove === 'ice') return 'win';
    if (opponentMove === 'paper') return 'lose';
  }
  if (opponentMove === 'steel') {
    if (playerMove === 'iron' || playerMove === 'rock' || playerMove === 'scissors' || playerMove === 'ice') return 'lose';
    if (playerMove === 'paper') return 'win';
  }
  
  if (playerMove === 'iron') {
    if (opponentMove === 'rock' || opponentMove === 'scissors') {
      return 'win';
    }
    if (opponentMove === 'paper') {
      return 'lose';
    }
  }

  if (opponentMove === 'iron') {
    if (playerMove === 'rock' || playerMove === 'scissors') {
      return 'lose';
    }
    if (playerMove === 'paper') {
      return 'win';
    }
  }

  if (
    (playerMove === 'rock' && opponentMove === 'scissors') ||
    (playerMove === 'paper' && opponentMove === 'rock') ||
    (playerMove === 'scissors' && opponentMove === 'paper')
  ) {
    return 'win';
  }
  
  return 'lose';
};

export const getRandomMove = (): Move => {
  return MOVES[Math.floor(Math.random() * MOVES.length)];
};

