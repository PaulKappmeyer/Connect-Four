package gamelogic;

import java.util.Arrays;

public class Gamelogic {

	public static final int INVALID_MOVE = -1;
	public static final int VALID_MOVE = 0;
	
	public static final int NOT_DROPPED = 0;
	public static final int RED = 1;
	public static final int YELLOW = -1;
	
	private int numRows;
	private int numColumns;
	private int numNeedForWin;
	private int[][] states;
	
	private boolean[] possibleMoves;
	private int[] rowDropIndices;
	
	private int[] winningRowIndices;
	private int[] winningColIndices;
	
	private int currentPlayer;
	private int movesPlayed;
	
	private boolean gameEndedInWin;
	private boolean gameEndedInDraw;
	private int playerWon;
	
	public Gamelogic(int numRows, int numColumns, int numNeedForWin) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.numNeedForWin = numNeedForWin;
		this.states = new int[numRows][numColumns];
		this.currentPlayer = RED;
		this.possibleMoves = new boolean[numColumns];
		this.rowDropIndices = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			possibleMoves[i] = true;
			rowDropIndices[i] = numRows - 1;
		}
		this.winningRowIndices = new int[numNeedForWin];
		this.winningColIndices = new int[numNeedForWin];
		this.currentPlayer = RED;
	}
	
	// copy constructor
	public Gamelogic(Gamelogic gamelogic) {
		this.numRows = gamelogic.getNumOfRows();
		this.numColumns = gamelogic.getNumOfColumns();
		this.numNeedForWin = gamelogic.getNumNeedForWin();
		this.states = gamelogic.getStates();
		this.possibleMoves = gamelogic.getPossibleMoves();
		this.rowDropIndices = gamelogic.getRowDropIndices();
		this.winningRowIndices = gamelogic.getWinningRowIndices();
		this.winningColIndices = gamelogic.getWinningColIndices();
		this.currentPlayer = gamelogic.getCurrentPlayer();
		this.movesPlayed = gamelogic.getNumOfMovesPlayed();
		this.gameEndedInDraw = gamelogic.didGameEndInDraw();
		this.gameEndedInWin = gamelogic.didGameEndInWin();
		this.playerWon = gamelogic.playerWon;
	}
	
	public void initNewGame() {
		currentPlayer = RED;
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			for (int colInd = 0; colInd < numColumns; colInd++) {
				states[rowInd][colInd] = NOT_DROPPED;
			}
		}
		for (int i = 0; i < numColumns; i++) {
			possibleMoves[i] = true;
			rowDropIndices[i] = numRows - 1;
		}
		movesPlayed = 0;
		gameEndedInWin = false;
		gameEndedInDraw = false;
		playerWon = NOT_DROPPED;
	}
	
	public int[] doMove(int columnIndex) {
		if (isPossibleMove(columnIndex) == false || gameEndedInWin || gameEndedInDraw) {
			return new int[]{INVALID_MOVE}; 
		}
		
		// get row index and construct return message with the information which grid position will be updated
		int rowIndex = rowDropIndices[columnIndex];
		int[] gridUpdateInfo = new int[] {VALID_MOVE, rowIndex, columnIndex, currentPlayer};
		
		// increment number of moves played
		movesPlayed++;
		
		// updated states
		states[rowIndex][columnIndex] = currentPlayer;
		
		// decrement the row dropping index
		rowDropIndices[columnIndex] --;
		if (rowDropIndices[columnIndex] < 0) {
			possibleMoves[columnIndex] = false;
		}
		
		// early exit: don't need to check for win or draw if not enough moves played
		if (movesPlayed < 2 * numNeedForWin - 1) {
			// switch current player
			switchPlayer();

			return gridUpdateInfo;
		}
		
		// else check for win or draw:
		checkForWinOrDraw();
		
		// switch current player
		switchPlayer();
		
		return gridUpdateInfo;
	}
	
	/**
	 * Switches the current player.
	 */
	private void switchPlayer() {
		switch (currentPlayer) {
		case RED:
			currentPlayer = YELLOW;
			break;

		case YELLOW:
			currentPlayer = RED;
			break;
		}
	}
	
	private void checkForWinOrDraw() {
		Object[] checkForWinInfo = checkForWin(states, currentPlayer, numNeedForWin);
		boolean hasWon = (boolean) checkForWinInfo[0];
		if (hasWon) {
			// set game finished
			gameEndedInWin = true;
			playerWon = currentPlayer;
			// set blink animation indices
			winningRowIndices = (int[]) checkForWinInfo[1];
			winningColIndices = (int[]) checkForWinInfo[2];
		} else if (movesPlayed == numColumns * numRows) { // check for draw
			// set game finished
			gameEndedInDraw = true;
		}
	}
	
	public boolean[] getPossibleMoves() {
		return possibleMoves.clone();
	}
	
	public int[] getRowDropIndices() {
		return rowDropIndices.clone();
	}
	
	public boolean isPossibleMove(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= numColumns) {
			return false;
		}
		return possibleMoves[columnIndex];
	}
	
	public static Object[] checkForWin(int[][] states, int player, int numNeedForWin) {
		int numRows = states.length;
		int numColumns = states[0].length;
		int[] winningRowIndices = new int[numNeedForWin];
		int[] winningColIndices = new int[numNeedForWin];

		// Check for horizontal row
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			outer: for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
				// clear arrays
				clearWinningIndicesArray(winningRowIndices, winningColIndices, numNeedForWin);

				for (int off = 0; off < numNeedForWin; off++) {
					int newColInd = colInd + off;
					if (states[rowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = rowInd;
					winningColIndices[off] = newColInd;
				}

				return new Object[] {true, winningRowIndices, winningColIndices};
			}
		}

		// Check for vertical row
		for (int colInd = 0; colInd < numColumns; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray(winningRowIndices, winningColIndices, numNeedForWin);

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + off;
					if (states[newRowInd][colInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = colInd;
				}
				return new Object[] {true, winningRowIndices, winningColIndices};
			}
		}

		// Check for diagonal row: down and right
		for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray(winningRowIndices, winningColIndices, numNeedForWin);

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + off;
					int newColInd = colInd + off;
					if (states[newRowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = newColInd;
				}
				return new Object[] {true, winningRowIndices, winningColIndices};
			}
		}

		// Check for diagonal row: up and right
		for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray(winningRowIndices, winningColIndices, numNeedForWin);

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + (numNeedForWin - 1) - off;
					int newColInd = colInd + off;

					if (states[newRowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = newColInd;
				}
				return new Object[] {true, winningRowIndices, winningColIndices};
			}
		}
		
		return new Object[] {false};
	}

	private static void clearWinningIndicesArray(int[] winningRowIndices, int[] winningColIndices, int numNeedForWin) {
		for (int i = 0; i < numNeedForWin; i++) {
			winningRowIndices[i] = -1;
			winningColIndices[i] = -1;
		}
	}
	
	public int[] getWinningRowIndices() {
		return winningRowIndices;
	}

	public int[] getWinningColIndices() {
		return winningColIndices;
	}
	
	public void printBoard() {
		printBoard(states);
	}
	
	public static void printBoard(int[][] states) {
		for (int[] stateRow : states) {
			for (int state : stateRow) {
				System.out.printf("%3d", state);
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public int[][] getStates(){
		return Arrays.stream(states).map(int[]::clone).toArray(int[][]::new);
	}
	
	public boolean didGameEndInWin() {
		return gameEndedInWin;
	}
	
	public boolean didGameEndInDraw() {
		return gameEndedInDraw;
	}
	
	public boolean didGameEnd() {
		return didGameEndInWin() || didGameEndInDraw();
	}
	
	public boolean didPlayerWin(int player) {
		return playerWon == player;
	}
	
	public int getPlayerWon() {
		return playerWon;
	}
	
	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public int getNumOfMovesPlayed() {
		return movesPlayed;
	}
	
	public int getNumOfRows() {
		return numRows;
	}
	
	public int getNumOfColumns() {
		return numColumns;
	}
	
	public int getNumNeedForWin() {
		return numNeedForWin;
	}
}
