package gamelogic;

import java.util.HashMap;

import gamelogic.Gamelogic.Boardstate;

public class MinimaxBot extends Bot implements Runnable {

	private final int maxDepth = 11;
	private int bestMove;
	private boolean nextMoveReady;
	
	private Gamelogic gamelogic;
	
	// multithreading 
	private volatile Thread minimaxThread;
	
	// moves in the middle of the board are more likely to be good -> search them first
	private static final int[] STANDARD_SEARCH_ORDER = new int[] {3, 2, 4, 1, 5, 0, 6};
	
	// transposition table -> look up moves that have already been calculated
	private HashMap<Long, Integer> transpositionTable;
	
	public MinimaxBot(Gamelogic gamelogic) {
		this.gamelogic = gamelogic;
		transpositionTable = new HashMap<Long, Integer>();
	}
	
	public void initNewGame() {
		nextMoveReady = false;
	}
	
	public int minimax(Gamelogic position, int depth, int alpha, int beta, boolean maximizingPlayer) {
		if (depth == 0 || position.didGameEnd()) {
			return evaluatePosition(position);
		}

		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			for (int columnIndex : STANDARD_SEARCH_ORDER) {
				// is valid move?
				if (position.doMove(columnIndex) == false) {
					continue;
				}
				int eval = minimax(position, depth-1, alpha, beta, false);
				position.undoLastMove();
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
			int minEval = Integer.MAX_VALUE;
			for (int columnIndex : STANDARD_SEARCH_ORDER) {
				// is valid move?
				if (position.doMove(columnIndex) == false) {
					continue;
				}
				int eval = minimax(position, depth-1, alpha, beta, true);
				position.undoLastMove();
				minEval = Math.min(minEval, eval);
				beta = Math.min(beta, eval);
			}
			return minEval;
		}
	}
	
	//here is where the evaluation is called
	public int evaluatePosition(Gamelogic position) {
		long hash = position.getBoardHash();
		if (transpositionTable.containsKey(hash)) {
			return transpositionTable.get(hash);
		}
		
		if (position.didPlayerWin(Boardstate.YELLOW)) {
			transpositionTable.put(hash, Integer.MAX_VALUE);
			return Integer.MAX_VALUE;
		} else if (position.didPlayerWin(Boardstate.RED)) {
			transpositionTable.put(hash, Integer.MIN_VALUE);
			return Integer.MIN_VALUE;
		} else if (position.didGameEndInDraw()) {
			transpositionTable.put(hash, 0);
			return 0;
		}

		transpositionTable.put(hash, position.getBoardEvaluation());
		return position.getBoardEvaluation();
	}
	
	@Override
	public int getNextMove(Gamelogic position) {
		if (nextMoveReady == false) {
			return -1;
		}
		nextMoveReady = false;
		return bestMove;
	}
	
	@Override
	public boolean isNextMoveReady() {
		return nextMoveReady;
	}
	
	public void start() {
		minimaxThread = new Thread(this);
		minimaxThread.start();
		nextMoveReady = false;
	}
	
	public void stop() {
		minimaxThread = null;
	}
	
	private int searchKillerMoves(Gamelogic position, Boardstate player) {
		for (int columnIndex = 0; columnIndex < gamelogic.getNumOfColumns(); columnIndex++) {
			// is valid move?
			if (position.doMove(columnIndex) == false) {
				continue;
			}
			if (position.didPlayerWin(player)) {
				position.undoLastMove();
				return columnIndex;
			}
			position.undoLastMove();
		}
		return -1;
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
			Gamelogic position = new Gamelogic(gamelogic);
			
			// search instantly winning moves for yellow
			bestMove = searchKillerMoves(position, Boardstate.YELLOW);
			if (bestMove != -1) {
				nextMoveReady = true;
				continue;
			}
			
			// search instantly winning moves for red
			position.switchPlayer();
			bestMove = searchKillerMoves(position, Boardstate.RED);
			position.switchPlayer();
			if (bestMove != -1) {
				nextMoveReady = true;
				continue;
			}
			
			// otherwise search with minimax
			int maxEval = minimax(position, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, true);
			long endTime = System.currentTimeMillis();
			
			System.out.println(" minmax found best move: " + bestMove);
			System.out.println("  with evaluation score: " + maxEval);
			System.out.println("            time needed: " + (endTime - startTime) + "ms");
			gamelogic.printBoard();
			
			// if minimax found that it will loose anyway, just pick the move with the best score for itself
			if (maxEval == Integer.MIN_VALUE) {
				System.out.println("minimax ran into a trap...");
				bestMove = getPossibleRandomMove(gamelogic);
			}

			nextMoveReady = true;
		}
	}
}
