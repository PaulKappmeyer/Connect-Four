package gamelogic;

public class MinimaxBot extends Bot {

	private static final int[][] evaluationTable = {
			{3, 4, 5, 7, 5, 4, 3}, 
			{4, 6, 8, 10, 8, 6, 4},
			{5, 8, 11, 13, 11, 8, 5}, 
			{5, 8, 11, 13, 11, 8, 5},
			{4, 6, 8, 10, 8, 6, 4},
			{3, 4, 5, 7, 5, 4, 3}};

	private final int maxDepth = 5;
	private int bestMove;
	
	public double minimax(Gamelogic position, int depth, double alpha, double beta, boolean maximizingPlayer) {
		if (depth == 0 || position.didGameEnd()) {
			return evaluatePosition(position);
		}

		if (maximizingPlayer) {
			double maxEval = Double.NEGATIVE_INFINITY;
			for (int columnIndex = 0; columnIndex < position.getNumOfColumns(); columnIndex++) {
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
			for (int columnIndex = 0; columnIndex < position.getNumOfColumns(); columnIndex++) {
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
		if (position.didPlayerWin(Gamelogic.YELLOW)) {
			return Double.POSITIVE_INFINITY;
			//			return Double.NEGATIVE_INFINITY;
		} else if (position.didPlayerWin(Gamelogic.RED)) {
			//			return Double.POSITIVE_INFINITY;
			return Double.NEGATIVE_INFINITY;
		} else if (position.didGameEndInDraw()) {
			return 0;
		}

		
		int utility = 138;
		int sum = 0;
		int[][] board = position.getStates();
		int numRows = board.length;
		int numColumns = board[0].length;
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				if (board[i][j] == Gamelogic.NOT_DROPPED) {
					continue;
				} else if (board[i][j] == Gamelogic.YELLOW) {
					sum += evaluationTable[i][j];

				} else if (board[i][j] == Gamelogic.RED) {
					sum -= evaluationTable[i][j];
				}
			}
		}
		return utility + sum;
	}

	@Override
	public int getNextMove(Gamelogic position) {
		// search best move with minimax
		bestMove = -1;
		
		double maxEval = minimax(position, maxDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
		
		System.out.println(" minmax found best move: " + bestMove);
		System.out.println("  with evaluation score: " + maxEval);
		
		// if minimax found that it will loose anyway, just pick the move with the best score for itself
		if (maxEval == Double.NEGATIVE_INFINITY) {
			System.out.println("minimax ran into a trap...");
			return getPossibleRandomMove(position);
		}
		
		return bestMove;
	}
}
