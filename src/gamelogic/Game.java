package gamelogic;

import java.awt.Graphics;
import java.util.Random;

import gamelogic.Gamelogic.Boardstate;
import visuals.Grid;
import visuals.InfoText;

public class Game {

	private static final Random rand = new Random();

	public enum Gamemode {
		PLAYER_VS_PLAYER,
		PLAYER_VS_COMPUTER,
		AUTO_DROP,
	}

	private Gamemode gamemode;

	private enum Gamestate {
		PLAYING,
		MATCH_ENDED,
		READY_FOR_RESET,
		IN_RESET_ANIMATION,
	}

	private Gamestate gamestate;

	private Gamelogic gamelogic;
	private Bot currentBot;
	private MinimaxBot minimaxBot;
	private SimpleBot simpleBot;
	private Grid grid;
	private InfoText infoText;

	private int numOfGamesPlayed;
	private int numOfDraws;
	private int redScore;
	private int yellowScore;

	// used for auto drop mode
	private double timeSinceLastDrop;
	private final double dropTime = 0.075;


	public Game(int numRows, int numColumns, int numNeedForWin) {
		gamelogic = new Gamelogic(numRows, numColumns, numNeedForWin);
		gamemode = Gamemode.PLAYER_VS_PLAYER;
		gamestate = Gamestate.PLAYING;

		// visuals
		grid = new Grid(numRows, numColumns, numNeedForWin);
		infoText = new InfoText(this);

		// computer AI
		minimaxBot = new MinimaxBot(gamelogic);
		simpleBot = new SimpleBot();

		initNewGame();
	}

	/**
	 * This function starts a new game with the current player being red
	 */
	private void initNewGame() {
		gamestate = Gamestate.PLAYING;
		gamelogic.initNewGame();
		grid.initNewGame();
		minimaxBot.initNewGame();
	}

	public void draw(Graphics graphics) {
		grid.draw(graphics);

		// draw text
		infoText.draw(graphics);
	}

	public void update(double tslf) {
		grid.update(tslf);

		switch (gamestate) {
		case PLAYING:
			switch (gamemode) {
			case PLAYER_VS_PLAYER:
				break;

			case AUTO_DROP:
				timeSinceLastDrop += tslf;
				if (timeSinceLastDrop > dropTime) {
					timeSinceLastDrop -= dropTime;

					int columnIndex = -1; 
					while (gamelogic.isPossibleMove(columnIndex) == false) {
						columnIndex = rand.nextInt(gamelogic.getNumOfColumns());
					}
					doMove(columnIndex, false);
				}
				break;

			case PLAYER_VS_COMPUTER:
				if (gamelogic.getCurrentPlayer() == Boardstate.YELLOW) {
					if (currentBot.isNextMoveReady()) {
						doMove(currentBot.getNextMove(gamelogic), false);				
					}
				}
				break;
			}
			break;

		case MATCH_ENDED:
			if (grid.isAnyCoinInDropAnimation() == false) {
				gamestate = Gamestate.READY_FOR_RESET;
			}
			break;

		case READY_FOR_RESET:
			switch (gamemode) {
			case AUTO_DROP:
				startResetAnimation();

			default:
				break;
			}
			break;

		case IN_RESET_ANIMATION:
			if (grid.isAnyCoinInResetAnimation() == false) {
				initNewGame();
			}
			break;
		}
	}

	public void doMove(int columnIndex, boolean humanInput) {
		if (gamestate != Gamestate.PLAYING) {
			return;
		}
		if (gamemode == Gamemode.PLAYER_VS_COMPUTER && humanInput && gamelogic.getCurrentPlayer() == Boardstate.YELLOW) {
			return;
		}

		Boardstate currentPlayer = gamelogic.getCurrentPlayer();
		boolean validMove = gamelogic.doMove(columnIndex);
		if (validMove == false) {
			return;
		}

		// update visuals
		int rowInd = gamelogic.getRowDropIndex(columnIndex) + 1;

		grid.setState(rowInd, columnIndex, currentPlayer);

		// game won or draw?
		if (gamelogic.didGameEnd()) {
			gamestate = Gamestate.MATCH_ENDED;

			if (gamelogic.didGameEndInWin()) {
				// start animation
				grid.startBlinkAnimation(gamelogic.getWinningRowIndices(), gamelogic.getWinningColIndices());
			}
		}

	}

	public void undoLastMove() {
		if (gamestate == Gamestate.IN_RESET_ANIMATION || gamelogic.getNumOfMovesPlayed() <= 0) {
			return;
		}

		//		// update state of game
		gamestate = Gamestate.PLAYING;

		// save column index
		int colInd = gamelogic.getLastMove();

		// undo move in logic
		gamelogic.undoLastMove();

		// undo move in visuals
		int rowInd = gamelogic.getRowDropIndex(colInd);
		grid.setState(rowInd, colInd, Boardstate.NOT_DROPPED);
		grid.stopBlinkAnimation();
	}

	public boolean isReadyForReset() {
		return gamestate == Gamestate.READY_FOR_RESET;
	}

	/**
	 * This function instantly starts the reset-animation
	 */
	public void startResetAnimation() {
		if (gamestate == Gamestate.IN_RESET_ANIMATION) {
			return;
		}

		// increment number of games played
		numOfGamesPlayed++;
		if (gamelogic.didGameEndInWin()) {
			// increment points
			switch (gamelogic.getPlayerWon()) {
			case NOT_DROPPED:
				break;

			case RED:
				redScore++;
				break;

			case YELLOW:
				yellowScore++;
				break;
			}
		} else if (gamelogic.didGameEndInDraw()) {
			numOfDraws++;
		}

		gamestate = Gamestate.IN_RESET_ANIMATION;
		grid.startResetAnimation();
	}

	public void toggleAutoDropMode() {
		if (gamemode == Gamemode.AUTO_DROP) {
			gamemode = Gamemode.PLAYER_VS_PLAYER;
		} else {
			gamemode = Gamemode.AUTO_DROP;
		}
	}

	public void toggleSingleplayerMode() {
		if (gamemode == Gamemode.PLAYER_VS_COMPUTER) {
			gamemode = Gamemode.PLAYER_VS_PLAYER;
			minimaxBot.stop();
			currentBot = null;

		} else {
			gamemode = Gamemode.PLAYER_VS_COMPUTER;

			// minimax bot is atm only implemented for the standard 6x7-grid
			if (gamelogic.getNumOfRows() <= Main.STANDARD_NUM_ROWS && gamelogic.getNumOfColumns() <= Main.STANDARD_NUM_COLUMNS) {
				minimaxBot.start();
				currentBot = minimaxBot;
			} else {
				currentBot = simpleBot;
			}
		}
	}

	public int mouseXToColumnIndex(int mouseX) {
		return grid.mouseXToColumnIndex(mouseX);
	}

	public Gamemode getGamemode() {
		return gamemode;
	}

	public Boardstate getCurrentPlayer() {
		return gamelogic.getCurrentPlayer();
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

	public int getNumOfMovesPlayed() {
		return gamelogic.getNumOfMovesPlayed();
	}
}
