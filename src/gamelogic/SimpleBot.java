package gamelogic;

import java.util.List;

import gamelogic.Gamelogic.Boardstate;

import java.util.ArrayList;
import java.util.Arrays;

public class SimpleBot extends Bot {
	
	@Override
	public int getNextMove(Gamelogic position) {
		int[] rowDropIndices = position.getRowDropIndices();
		Boardstate[][] board = position.getBoard();
		
		// check own winning moves
		int[] ownWinningMoves = seachWinningMoves(position, board, rowDropIndices, Boardstate.YELLOW);
		if (ownWinningMoves.length > 0) {
			return ownWinningMoves[0];
		}
		
		// check opponents winning moves
		int[] opponentsWinningMoves = seachWinningMoves(position, board, rowDropIndices, Boardstate.RED);
		if (opponentsWinningMoves.length > 0) {
			return opponentsWinningMoves[0];
		}
		
		// search a good move:
		int columnIndex;
		boolean badMove;
		boolean[] instantlyLosing = new boolean[position.getNumOfColumns()];
		do {
		// try a random move
		columnIndex = getPossibleRandomMove(position);
		// no chance?
		if (Arrays.equals(instantlyLosing, position.getPossibleMoves())) {
			return columnIndex;
		}
		badMove = false;
		// was move already checked as losing?
		if (instantlyLosing[columnIndex]) {
			badMove = true;
			continue;
		}
		
		// apply random move to board
		int rowIndex = rowDropIndices[columnIndex];
		board[rowIndex][columnIndex] = Boardstate.YELLOW;
		rowDropIndices[columnIndex] --;
		
		// check opponents winning moves
		opponentsWinningMoves = seachWinningMoves(position, board, rowDropIndices, Boardstate.RED);
		
		// does move allow red to win? 
		if (opponentsWinningMoves.length > 0) {
//			System.out.println("Red could then win with : " + Arrays.toString(opponentsWinningMoves));
			badMove = true;
			instantlyLosing[columnIndex] = true;
			
			// undo move
			board[rowIndex][columnIndex] = Boardstate.NOT_DROPPED;
			rowDropIndices[columnIndex] ++;
		}
		
		} while (badMove == true);
		
		return columnIndex;
	}
	
	private int[] seachWinningMoves(Gamelogic position, Boardstate[][] board, int[] rowDropIndices, Boardstate player) {
		List<Integer> winningMoves = new ArrayList<>();
		
		for (int columnIndex = 0; columnIndex < position.getNumOfColumns(); columnIndex++) {
			// is move possible?
			int rowIndex = rowDropIndices[columnIndex]; 
			if (rowIndex < 0) {
				continue;
			}
			// apply move
			board[rowIndex][columnIndex] = player;
			
			// check for win:
			boolean hasWon = (boolean) Gamelogic.checkForWin(board, player, position.getNumNeedForWin())[0];
			if (hasWon) {
				winningMoves.add(columnIndex);
			}
			
			// undo move
			board[rowIndex][columnIndex] = Boardstate.NOT_DROPPED;
		}
		
		return winningMoves.stream().mapToInt(i -> i).toArray();
	}
}