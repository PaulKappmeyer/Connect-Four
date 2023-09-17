package gamelogic;

import gamelogic.Gamelogic.Boardstate;

public class MinimaxBot extends Bot implements Runnable {

	private static final int[][] evaluationTable = {
			{3, 4, 5, 7, 5, 4, 3}, 
			{4, 6, 8, 10, 8, 6, 4},
			{5, 8, 11, 13, 11, 8, 5}, 
			{5, 8, 11, 13, 11, 8, 5},
			{4, 6, 8, 10, 8, 6, 4},
			{3, 4, 5, 7, 5, 4, 3}};

	private final int maxDepth = 11;
	private int bestMove;
	private boolean nextMoveReady;
	
	private Gamelogic gamelogic;
	
	private volatile Thread minimaxThread;
	
	private int[] columnSearchOrder = new int[] {3, 2, 4, 1, 5, 0, 6};
	
	public MinimaxBot(Gamelogic gamelogic) {
		this.gamelogic = gamelogic;
	}
	
	public void initNewGame() {
		nextMoveReady = false;
	}
	
	public double minimax(Gamelogic position, int depth, double alpha, double beta, boolean maximizingPlayer) {
		if (depth == 0 || position.didGameEnd()) {
			return evaluatePosition(position);
		}

		if (maximizingPlayer) {
			double maxEval = Double.NEGATIVE_INFINITY;
			for (int columnIndex : columnSearchOrder) {
				if (position.isPossibleMove(columnIndex) == false) {
					continue;
				}
				Gamelogic child = new Gamelogic(position);
				child.doMove(columnIndex);
				double eval = minimax(child, depth-1, alpha, beta, false);
//				maxEval = Math.max(maxEval, eval);			
				if (eval > maxEval) {
					maxEval = eval;
					if (depth == maxDepth) {
						bestMove = columnIndex;
					}
				}
				alpha = Math.max(alpha, eval);
				if (beta <= alpha) {
					break;
				}
			}
			return maxEval;
		} else {
			double minEval = Double.POSITIVE_INFINITY;
			for (int columnIndex : columnSearchOrder) {
				if (position.isPossibleMove(columnIndex) == false) {
					continue;
				}
				Gamelogic child = new Gamelogic(position);
				child.doMove(columnIndex);
				double eval = minimax(child, depth-1, alpha, beta, true);
				minEval = Math.min(minEval, eval);
				beta = Math.min(beta, eval);
			}
			return minEval;
		}
	}

	//here is where the evaluation table is called
	public double evaluatePosition(Gamelogic position) {
		if (position.didPlayerWin(Boardstate.YELLOW)) {
			return Double.POSITIVE_INFINITY;
		} else if (position.didPlayerWin(Boardstate.RED)) {
			return Double.NEGATIVE_INFINITY;
		} else if (position.didGameEndInDraw()) {
			return 0;
		}

		
		int utility = 138;
		double sum = 0;
		Boardstate[][] board = position.getBoard();
		int numRows = board.length;
		int numColumns = board[0].length;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				switch (board[i][j]) {
				case NOT_DROPPED:
					continue;
				
				case YELLOW:
					sum += evaluationTable[i][j];
					break;
					
				case RED:
					sum -= evaluationTable[i][j];
					break;
				}
			}
		}
		return utility + sum;
	}

	@Override
	public int getNextMove(Gamelogic position) {
		if (nextMoveReady == false) {
			return -1;
		}
		nextMoveReady = false;
		return bestMove;
	}
	
	public void start() {
		minimaxThread = new Thread(this);
		minimaxThread.start();
	}
	
	public void stop() {
		minimaxThread = null;
	}
	
	@Override
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (minimaxThread == thisThread) {
			// early exit: current move is not the computers move
			if (gamelogic.getCurrentPlayer() == Boardstate.RED || nextMoveReady) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}

			// search best move with minimax, minimax function updates bestMove
			bestMove = -1;
			long startTime = System.currentTimeMillis();
			double maxEval = minimax(gamelogic, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
			long endTime = System.currentTimeMillis();
			
			System.out.println(" minmax found best move: " + bestMove);
			System.out.println("  with evaluation score: " + maxEval);
			System.out.println("            time needed: " + (endTime - startTime) + "ms");
			gamelogic.printBoard();
			
			// if minimax found that it will loose anyway, just pick the move with the best score for itself
			if (maxEval == Double.NEGATIVE_INFINITY) {
				System.out.println("minimax ran into a trap...");
				bestMove = getPossibleRandomMove(gamelogic);
			}

			nextMoveReady = true;
		}
	}
}
