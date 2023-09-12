package gamelogic;

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

	private int[] rowDropIndices;

	private final int[] winningRowIndices;
	private final int[] winningColIndices;

	private int currentPlayer;

	private int numOfGamesPlayed;
	private int numOfDraws;
	private int redScore;
	private int yellowScore;
	private int movesPlayed;

	private boolean gameFinished;
	private boolean gameFinishedInDraw;
	private int winningPlayer;
	
	public Gamelogic(int numRows, int numColumns, int numNeedForWin) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.numNeedForWin = numNeedForWin;
		this.states = new int[numRows][numColumns];
		this.rowDropIndices = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			rowDropIndices[i] = numRows - 1;
		}
		this.winningColIndices = new int[numNeedForWin];
		this.winningRowIndices = new int[numNeedForWin];
		this.currentPlayer = RED;
	}

	public void initNewGame() {
		currentPlayer = RED;
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			for (int colInd = 0; colInd < numColumns; colInd++) {
				states[rowInd][colInd] = NOT_DROPPED;
			}
		}
		for (int i = 0; i < numColumns; i++) {
			rowDropIndices[i] = numRows - 1;
		}
		movesPlayed = 0;
		gameFinished = false;
		gameFinishedInDraw = false;
		winningPlayer = NOT_DROPPED;
	}

	public int[] doMove(int columnIndex) {
		if (isPossibleMove(columnIndex) == false || gameFinished) {
			return new int[]{INVALID_MOVE};
		}

		int rowIndex = rowDropIndices[columnIndex];
		int[] returnMsg = new int[] {VALID_MOVE, rowIndex, columnIndex, currentPlayer};

		// increment number of moves played
		movesPlayed++;

		// updated states
		states[rowIndex][columnIndex] = currentPlayer;

		// decrement the row dropping index
		rowDropIndices[columnIndex] --;

		// early exit: don't need to check for win or draw if not enough moves played
		if (movesPlayed < 2 * numNeedForWin - 1) {
			// switch current player
			switchPlayer();

			return returnMsg;
		}

		// check for win
		if (checkForWin(currentPlayer, numNeedForWin)) {
			// set game finished
			gameFinished = true;
			winningPlayer = currentPlayer;
			// increment points
			switch (currentPlayer) {
			case RED:
				redScore++;
				break;

			case YELLOW:
				yellowScore++;
				break;
			}
			// increment number of games played
			numOfGamesPlayed++;
		} else if (movesPlayed == numColumns * numRows) { // check for draw
			// set game finished 
			gameFinished = true;
			gameFinishedInDraw = true;
			// increment number of games played
			numOfGamesPlayed++;
			numOfDraws++;
		}

		// switch current player
		switchPlayer();

		return returnMsg;
	}

	public boolean isPossibleMove(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= numColumns) {
			return false;
		}
		return rowDropIndices[columnIndex] >= 0;
	}

	public boolean isGameFinished() {
		return gameFinished;
	}
	
	public boolean isGameFinishedInDraw() {
		return gameFinishedInDraw;
	}
	
	public boolean didPlayerWin(int player) {
		return winningPlayer == player;
	}
	
	/**
	 * Switches the current player
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

	/**
	 * Checks if the current turn relates into a win for the current player
	 * @param player the color to check a win for, often it is the color of the current turn
	 * @return true if the current player has won, false if not
	 */
	public boolean checkForWin(int player, int numNeedForWin) {
		// Check for horizontal row
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			outer: for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
				// clear arrays
				clearWinningIndicesArray();

				for (int off = 0; off < numNeedForWin; off++) {
					int newColInd = colInd + off;
					if (states[rowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = rowInd;
					winningColIndices[off] = newColInd;
				}

				return true;
			}
		}

		// Check for vertical row
		for (int colInd = 0; colInd < numColumns; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray();

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + off;
					if (states[newRowInd][colInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = colInd;
				}
				return true;
			}
		}

		// Check for diagonal row: down and right
		for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray();

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + off;
					int newColInd = colInd + off;
					if (states[newRowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = newColInd;
				}
				return true;
			}
		}

		// Check for diagonal row: up and right
		for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
			outer: for (int rowInd = 0; rowInd <= numRows - numNeedForWin; rowInd++) {
				// clear arrays
				clearWinningIndicesArray();

				for (int off = 0; off < numNeedForWin; off++) {
					int newRowInd = rowInd + (numNeedForWin - 1) - off;
					int newColInd = colInd + off;

					if (states[newRowInd][newColInd] != player) {
						continue outer;
					}

					winningRowIndices[off] = newRowInd;
					winningColIndices[off] = newColInd;
				}
				return true;
			}
		}

		// clear arrays
		clearWinningIndicesArray();
		return false;
	}

	private void clearWinningIndicesArray() {
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
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			for (int colInd = 0; colInd < numColumns; colInd++) {
				System.out.printf("%3d", states[rowInd][colInd]);
			}
			System.out.println();
		}
	}

	public int getNumOfColumns() {
		return numColumns;
	}

	public int getCurrentPlayer() {
		return currentPlayer;
	}

	public int getNumOfMovesPlayed() {
		return movesPlayed;
	}

	public int getRedScore() {
		return redScore;
	}

	public int getYellowScore() {
		return yellowScore;
	}

	public int getNumOfDraws() {
		return numOfDraws;
	}

	public int getNumOfGamesPlayed() {
		return numOfGamesPlayed;
	}
}
