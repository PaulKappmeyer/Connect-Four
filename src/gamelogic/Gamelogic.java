package gamelogic;

import java.util.Arrays;

public class Gamelogic {
	
	public enum Boardstate {
		NOT_DROPPED,
		RED,
		YELLOW
	}
	
	private final int numRows;
	private final int numColumns;
	private final int numNeedForWin;
	private final Boardstate[][] board;
	private final int maxNumOfMoves;
	private final boolean isStandardSize;
	
	private final boolean[] possibleMoves;
	private final int[] rowDropIndices;
	
	private int[] winningRowIndices;
	private int[] winningColIndices;
	
	private Boardstate currentPlayer;
	private int movesPlayed;
	
	private final int[] moveHistory;
	
	private boolean gameEndedInWin;
	private boolean gameEndedInDraw;
	private Boardstate playerWon;
	
	// used for minimax
	private static final int EVALUATION_UTILITY = 138;
	private static final int[][] evaluationTable = {
			{3, 4, 5, 7, 5, 4, 3}, 
			{4, 6, 8, 10, 8, 6, 4},
			{5, 8, 11, 13, 11, 8, 5}, 
			{5, 8, 11, 13, 11, 8, 5},
			{4, 6, 8, 10, 8, 6, 4},
			{3, 4, 5, 7, 5, 4, 3}};
	private int positionEvaluation;
	
	private static final byte[] POWERS_OF_TWO = new byte[] {1, 2, 4, 8, 16, 32};
	private long boardHash;
	private final byte[] colHashes;
	
	public Gamelogic(int numRows, int numColumns, int numNeedForWin) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.isStandardSize = numRows == Main.STANDARD_NUM_ROWS && numColumns == Main.STANDARD_NUM_COLUMNS;
		this.numNeedForWin = numNeedForWin;
		this.maxNumOfMoves = numRows * numColumns;
		
		this.board = new Boardstate[numRows][numColumns];
		this.possibleMoves = new boolean[numColumns];
		this.rowDropIndices = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			possibleMoves[i] = true;
			rowDropIndices[i] = numRows - 1;
		}
		this.winningRowIndices = new int[numNeedForWin];
		this.winningColIndices = new int[numNeedForWin];
		this.currentPlayer = Boardstate.RED;
		this.moveHistory = new int[maxNumOfMoves];
		for (int i = 0; i < maxNumOfMoves; i++) {
			moveHistory[i] = -1;
		}
		this.colHashes = new byte[numColumns];
	}
	
	// copy constructor
	public Gamelogic(Gamelogic gamelogic) {
		// primitive types
		this.numRows = gamelogic.numRows;
		this.numColumns = gamelogic.numColumns;
		this.isStandardSize = gamelogic.isStandardSize;
		this.numNeedForWin = gamelogic.numNeedForWin;
		this.maxNumOfMoves = gamelogic.maxNumOfMoves;
		this.movesPlayed = gamelogic.movesPlayed;
		this.currentPlayer = gamelogic.currentPlayer;
		this.playerWon = gamelogic.playerWon;
		this.gameEndedInDraw = gamelogic.gameEndedInDraw;
		this.gameEndedInWin = gamelogic.gameEndedInWin;
		this.positionEvaluation = gamelogic.positionEvaluation;
		
		// object types -> need copying
		this.board = gamelogic.getBoard();
		this.possibleMoves = gamelogic.getPossibleMoves();
		this.rowDropIndices = gamelogic.getRowDropIndices();
		this.winningRowIndices = gamelogic.getWinningRowIndices();
		this.winningColIndices = gamelogic.getWinningColIndices();
		this.moveHistory = gamelogic.getMoveHistory();
		this.colHashes = gamelogic.colHashes.clone();
	}
	
	public void initNewGame() {
		currentPlayer = Boardstate.RED;
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			for (int colInd = 0; colInd < numColumns; colInd++) {
				board[rowInd][colInd] = Boardstate.NOT_DROPPED;
			}
		}
		for (int i = 0; i < numColumns; i++) {
			possibleMoves[i] = true;
			rowDropIndices[i] = numRows - 1;
			colHashes[i] = 0;
			boardHash = 0;
		}
		movesPlayed = 0;
		gameEndedInWin = false;
		gameEndedInDraw = false;
		playerWon = Boardstate.NOT_DROPPED;
		positionEvaluation = EVALUATION_UTILITY;
	}
	
	// returns true if move is valid, false otherwise
	public boolean doMove(int columnIndex) {
		if (isPossibleMove(columnIndex) == false || gameEndedInWin || gameEndedInDraw) {
			return false;
		}
		
		// get row index and construct return message with the information which grid position will be updated
		int rowIndex = rowDropIndices[columnIndex];
		
		// keep track of the history
		moveHistory[movesPlayed] = columnIndex;
		
		// increment number of moves played
		movesPlayed++;
		
		// updated the board
		board[rowIndex][columnIndex] = currentPlayer;
		
		// update evaluation and hash
		if (isStandardSize()) {
			updateBoardEvaluation(rowIndex, columnIndex, currentPlayer);
			updateBoardHash(rowIndex, columnIndex, currentPlayer);
		}
		
		// decrement the row dropping index
		rowDropIndices[columnIndex]--;
		if (rowDropIndices[columnIndex] < 0) {
			possibleMoves[columnIndex] = false;
		}
		
		// early exit: don't need to check for win or draw if not enough moves played
		if (movesPlayed < 2 * numNeedForWin - 1) {
			// switch current player
			switchPlayer();

			return true;
		}
		
		// else check for win or draw:
		checkForWinOrDraw();
		
		// switch current player
		switchPlayer();
		
		return true;
	}
	
	public void undoLastMove() {
		// set game to playing again:
		gameEndedInWin = false;
		gameEndedInDraw = false;
		playerWon = Boardstate.NOT_DROPPED;
		
		// get the last move
		int move = moveHistory[movesPlayed-1];
		
		// switch current player
		switchPlayer();
		
		// increment the row dropping index
		int rowIndex = ++rowDropIndices[move];
		possibleMoves[move] = true;
		
		// update evaluation and hash
		if (isStandardSize()) {
			updateBoardEvaluation(rowIndex, move, Boardstate.NOT_DROPPED);
			updateBoardHash(rowIndex, move, Boardstate.NOT_DROPPED);
		}
		
		// updated the board
		board[rowIndex][move] = Boardstate.NOT_DROPPED;
		
		// decrement the number of moves played
		movesPlayed--;
		
		// update the history
		moveHistory[movesPlayed] = -1;
	}
	
	// returns columnIndex
	public int getLastMove() {
		return moveHistory[movesPlayed-1];
	}
	
	public int[] getMoveHistory() {
		return moveHistory.clone();
	}
	
	private void updateBoardEvaluation(int rowIndex, int colIndex, Boardstate player) {
		switch (player) {
		case NOT_DROPPED:
			switch (board[rowIndex][colIndex]) {
			case NOT_DROPPED:
				break;
			
			case YELLOW:
				positionEvaluation -= evaluationTable[rowIndex][colIndex];
				break;
				
			case RED:
				positionEvaluation += evaluationTable[rowIndex][colIndex];
				break;
			}
			break;
		
		case YELLOW:
			positionEvaluation += evaluationTable[rowIndex][colIndex];
			break;
			
		case RED:
			positionEvaluation -= evaluationTable[rowIndex][colIndex];
			break;
		}
	}
	
	public int getBoardEvaluation() {
		return positionEvaluation;
	}	
	
	private void updateBoardHash(int rowIndex, int colIndex, Boardstate player) {
		boardHash -= ((long) colHashes[colIndex]) << (7 * (numColumns - colIndex));
		
		switch (player) {
		case NOT_DROPPED:
			switch (board[rowIndex][colIndex]) {
			case NOT_DROPPED:
				break;
			
			case YELLOW:
				colHashes[colIndex] -= 2 * POWERS_OF_TWO[numRows - 1 - rowIndex];
				break;
				
			case RED:
				colHashes[colIndex] -= 1 * POWERS_OF_TWO[numRows - 1 - rowIndex];
				break;
			}
			
			boardHash += ((long) colHashes[colIndex]) << (7 * (numColumns - colIndex));
			return;
			
		case YELLOW:
			colHashes[colIndex] += 2 * POWERS_OF_TWO[numRows - 1 - rowIndex];
			break;
		
		case RED:
			colHashes[colIndex] += 1 * POWERS_OF_TWO[numRows - 1 - rowIndex];
			break;
		}
		
		boardHash += ((long) colHashes[colIndex]) << (7 * (numColumns - colIndex));
	}
	
	public long getBoardHash() {
		return boardHash;
	}

	/**
	 * Switches the current player.
	 */
	public void switchPlayer() {
		switch (currentPlayer) {
		case NOT_DROPPED:
			break;
		
		case RED:
			currentPlayer = Boardstate.YELLOW;
			break;

		case YELLOW:
			currentPlayer = Boardstate.RED;
			break;
		}
	}
	
	private void checkForWinOrDraw() {
		Object[] checkForWinInfo = checkForWin(board, currentPlayer, numNeedForWin);
		boolean hasWon = (boolean) checkForWinInfo[0];
		if (hasWon) {
			// set game finished
			gameEndedInWin = true;
			playerWon = currentPlayer;
			// set blink animation indices
			winningRowIndices = (int[]) checkForWinInfo[1];
			winningColIndices = (int[]) checkForWinInfo[2];
		} else if (movesPlayed == maxNumOfMoves) { // check for draw
			// set game finished
			gameEndedInDraw = true;
		}
	}
	
	public boolean[] getPossibleMoves() {
		return possibleMoves.clone();
	}
	
	public int getRowDropIndex(int columnIndex) {
		if (columnIndex < 0 || columnIndex >= numColumns) {
			return -1;
		}
		return rowDropIndices[columnIndex];
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
	
	public boolean isStandardSize() {
		return isStandardSize;
	}
	
	public static Object[] checkForWin(Boardstate[][] board, Boardstate player, int numNeedForWin) {
		int numRows = board.length;
		int numColumns = board[0].length;
		int[] winningRowIndices = new int[numNeedForWin];
		int[] winningColIndices = new int[numNeedForWin];

		// Check for horizontal row
		for (int rowInd = 0; rowInd < numRows; rowInd++) {
			outer: for (int colInd = 0; colInd <= numColumns - numNeedForWin; colInd++) {
				// clear arrays
				clearWinningIndicesArray(winningRowIndices, winningColIndices, numNeedForWin);

				for (int off = 0; off < numNeedForWin; off++) {
					int newColInd = colInd + off;
					if (board[rowInd][newColInd] != player) {
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
					if (board[newRowInd][colInd] != player) {
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
					if (board[newRowInd][newColInd] != player) {
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

					if (board[newRowInd][newColInd] != player) {
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
		printBoard(board);
	}
	
	public static void printBoard(Boardstate[][] board) {
//		System.out.println(boardToString(board, " 0", " 1", "-1", " ", "\n"));
		System.out.println(boardToString(board, "O", "R", "Y", " ", "\n"));
	}
	
	public static String boardToString(Boardstate[][] board, String not_dropped, String red, String yellow, String sep, String rowSep) {
		StringBuilder str = new StringBuilder();
		for (Boardstate[] boardRow : board) {
			for (Boardstate state : boardRow) {
				switch (state) {
				case NOT_DROPPED:
					str.append(not_dropped);
					break;
				
				case RED:
					str.append(red);
					break;
					
				case YELLOW:
					str.append(yellow);
					break;
				}
				str.append(sep);
			}
			str.append(rowSep);
		}
		return str.toString(); 
	}
	
	public String toString(String not_dropped, String red, String yellow, String sep, String rowSep) {
		return boardToString(board, not_dropped, red, yellow, sep, rowSep);
	}
	
	@Override
	public String toString() {
//		return boardToString(board, " 0", " 1", "-1", " ", "\n");
		return boardToString(board, "O", "R", "Y", " ", "\n");
	}
	
	public Boardstate[][] getBoard(){
		return Arrays.stream(board).map(Boardstate[]::clone).toArray(Boardstate[][]::new);
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
	
	public boolean didPlayerWin(Boardstate player) {
		return playerWon == player;
	}
	
	public Boardstate getPlayerWon() {
		return playerWon;
	}
	
	public Boardstate getCurrentPlayer() {
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
