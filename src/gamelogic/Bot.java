package gamelogic;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Bot {

	private Gamelogic gamelogic;
	
	private static final Random rand = new Random();
	
	public Bot(Gamelogic gamelogic) {
		this.gamelogic = gamelogic;
	}
	
	public int getNextMove() {
//		gamelogic.printBoard();
		
		// check own winning moves
		int[] ownWinningMoves = seachWinningMoves(Gamelogic.YELLOW);
//		System.out.println("winning moves for yellow: " + Arrays.toString(ownWinningMoves));
		if (ownWinningMoves.length > 0) {
			return ownWinningMoves[0];
		}
		
		// check opponents winning moves
		int[] opponentsWinningMoves = seachWinningMoves(Gamelogic.RED);
//		System.out.println("   winning moves for red: " + Arrays.toString(opponentsWinningMoves));
		if (opponentsWinningMoves.length > 0) {
			return opponentsWinningMoves[0];
		}
		
		// just a random move
		int columnIndex;
		do {
			columnIndex = (int) rand.nextGaussian(gamelogic.getNumOfColumns()/2, 1);
		} while (gamelogic.isPossibleMove(columnIndex) == false);
		
		return columnIndex;
	}
	
	private int[] seachWinningMoves(int player) {
//		System.out.println("possible next boards: ");
		List<Integer> winningMoves = new ArrayList<>();
		
		int[] rowDropIndices = gamelogic.getRowDropIndices();
		for (int columnIndex = 0; columnIndex < gamelogic.getNumOfColumns(); columnIndex++) {
			// is move possible?
			if (rowDropIndices[columnIndex] < 0) {
				continue;
			}
			// apply move
			int[][] states = gamelogic.getStates();
			states[rowDropIndices[columnIndex]][columnIndex] = player;
			
//			Gamelogic.printBoard(states);
			
			// check for win:
			boolean hasWon = (boolean) Gamelogic.checkForWin(states, player, gamelogic.getNumNeedForWin())[0];
			if (hasWon) {
				winningMoves.add(columnIndex);
			}
		}
		
		return winningMoves.stream().mapToInt(i -> i).toArray();
	}
}
