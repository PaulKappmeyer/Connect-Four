package gamelogic;

import java.util.Random;

public class Simulator {

	private static final Random rand = new Random();

	public static void main(String[] args) {
		Gamelogic gamelogic = new Gamelogic(6, 7, 4);

		int simulateGames = 1000000;
		long movesPlayedSum = 0;
		long redScore = 0;
		long yellowScore = 0;
		long draws = 0;
		
		// simulate a bunch of matches
		for (int i = 0; i < simulateGames; i++) {
			gamelogic.initNewGame();

			// simulate a single match
			do {
				int columnIndex = -1; 
				while (gamelogic.isPossibleMove(columnIndex) == false) {
					columnIndex = rand.nextInt(gamelogic.getNumOfColumns());
				}
				gamelogic.doMove(columnIndex);
			} while (gamelogic.didGameEnd() == false);

			if (gamelogic.didPlayerWin(Gamelogic.RED)) {
				redScore ++;
			} else if (gamelogic.didPlayerWin(Gamelogic.YELLOW)) {
				yellowScore ++;
			} else if (gamelogic.didGameEndInDraw()) {
				draws ++;
			}
			
			movesPlayedSum += gamelogic.getNumOfMovesPlayed();
		}
		
		// print statistics
		System.out.println("   red score: " + redScore);
		System.out.println("yellow score: " + yellowScore);
		System.out.println("        draw: " + draws);
		System.out.println(" total games: " + simulateGames);
		System.out.println(" total moves: " + movesPlayedSum);
		System.out.println("avg moves till game finish: " + (double) movesPlayedSum / simulateGames);
	}

}
