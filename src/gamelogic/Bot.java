package gamelogic;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Bot {
	
	private static final Random rand = new Random();
	
	private Gamelogic gamelogic;
	
	public Bot(Gamelogic gamelogic) {
		this.gamelogic = gamelogic;
	}
	
	public int getNextMove() {
		int[] rowDropIndices = gamelogic.getRowDropIndices();
		int[][] states = gamelogic.getStates();
		
		// check own winning moves
		int[] ownWinningMoves = seachWinningMoves(states, rowDropIndices, Gamelogic.YELLOW);
		if (ownWinningMoves.length > 0) {
			return ownWinningMoves[0];
		}
		
		// check opponents winning moves
		int[] opponentsWinningMoves = seachWinningMoves(states, rowDropIndices, Gamelogic.RED);
		if (opponentsWinningMoves.length > 0) {
			return opponentsWinningMoves[0];
		}
		
		// search a good move:
		int columnIndex;
		boolean badMove;
		boolean[] instantlyLosing = new boolean[gamelogic.getNumOfColumns()];
		do {
		// try a random move
		columnIndex = getPossibleRandomMove();
		// no chance?
		if (Arrays.equals(instantlyLosing, gamelogic.getPossibleMoves())) {
			return columnIndex;
		}
		badMove = false;
		// was move already checked as losing?
		if (instantlyLosing[columnIndex]) {
			badMove = true;
			continue;
		}
		
		// apply random move to states
		int rowIndex = rowDropIndices[columnIndex];
		states[rowIndex][columnIndex] = Gamelogic.YELLOW;
		rowDropIndices[columnIndex] --;
		
		// check opponents winning moves
		opponentsWinningMoves = seachWinningMoves(states, rowDropIndices, Gamelogic.RED);
		
		// does move allow red to win? 
		if (opponentsWinningMoves.length > 0) {
			badMove = true;
			instantlyLosing[columnIndex] = true;
			
			// undo move
			states[rowIndex][columnIndex] = Gamelogic.NOT_DROPPED;
			rowDropIndices[columnIndex] ++;
		}
		
		} while (badMove == true);
		
		return columnIndex;
	}

	private int getPossibleRandomMove() {
		int columnIndex;
		do {
			columnIndex = (int) rand.nextGaussian(gamelogic.getNumOfColumns()/2, 1);
		} while (gamelogic.isPossibleMove(columnIndex) == false);
		return columnIndex;
	}
	
	private int[] seachWinningMoves(int[][] states, int[] rowDropIndices, int player) {
		List<Integer> winningMoves = new ArrayList<>();
		
		for (int columnIndex = 0; columnIndex < gamelogic.getNumOfColumns(); columnIndex++) {
			// is move possible?
			int rowIndex = rowDropIndices[columnIndex]; 
			if (rowIndex < 0) {
				continue;
			}
			// apply move
			states[rowIndex][columnIndex] = player;
			
			// check for win:
			boolean hasWon = (boolean) Gamelogic.checkForWin(states, player, gamelogic.getNumNeedForWin())[0];
			if (hasWon) {
				winningMoves.add(columnIndex);
			}
			
			// undo move
			states[rowIndex][columnIndex] = Gamelogic.NOT_DROPPED;
		}
		
		return winningMoves.stream().mapToInt(i -> i).toArray();
	}
}
