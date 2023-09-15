package gamelogic;

import java.awt.Graphics;
import java.util.Random;

import visuals.Coin;
import visuals.Grid;
import visuals.InfoText;

public class Game {

	private static final Random rand = new Random();
	
	public static final int TWO_PLAYER_MODE = 0;
	public static final int AUTO_DROP_MODE = 1;
	public static final int SINGLEPLAYER_MODE = 2;

	private int gamemode;
	
	private static final int PLAYING = 0;
	private static final int MATCH_ENDED = 1;
	private static final int READY_FOR_RESET = 2;
	private static final int IN_RESET_ANIMATION = 3;

	private int gamestate;

	private Gamelogic gamelogic;
	private MinimaxBot minimaxBot = new MinimaxBot();
	private SimpleBot simpleBot = new SimpleBot();
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
		gamemode = TWO_PLAYER_MODE;
		gamestate = PLAYING;
		
		// visuals
		grid = new Grid(numRows, numColumns, numNeedForWin);
		infoText = new InfoText(this);
		
		// computer AI
		minimaxBot = new MinimaxBot();
		simpleBot = new SimpleBot();
		
		initNewGame();
	}

	/**
	 * This function starts a new game with the current player being red
	 */
	private void initNewGame() {
		gamelogic.initNewGame();
		grid.initNewGame();
		gamestate = PLAYING;
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
			case TWO_PLAYER_MODE:
				break;

			case AUTO_DROP_MODE:
				timeSinceLastDrop += tslf;
				if (timeSinceLastDrop > dropTime) {
					timeSinceLastDrop -= dropTime;

					int columnIndex = -1; 
					while (gamelogic.isPossibleMove(columnIndex) == false) {
						columnIndex = rand.nextInt(gamelogic.getNumOfColumns());
					}
					doMove(columnIndex);
				}
				break;

			case SINGLEPLAYER_MODE:
				if (gamelogic.getCurrentPlayer() == Gamelogic.YELLOW) {
					timeSinceLastDrop += tslf;
					if (timeSinceLastDrop > dropTime) {
						timeSinceLastDrop -= dropTime;

						// minimax bot is atm only implemented for the standard 6x7-grid
						if (gamelogic.getNumOfRows() <= Main.STANDARD_NUM_ROWS && gamelogic.getNumOfColumns() <= Main.STANDARD_NUM_COLUMNS) {
							doMove(minimaxBot.getNextMove(gamelogic));
						} else {
							doMove(simpleBot.getNextMove(gamelogic));
						}
						
					}
				}
				break;
			}
			break;

		case MATCH_ENDED:
			if (grid.isAnyCoinInDropAnimation() == false) {
				gamestate = READY_FOR_RESET;
			}
			break;

		case READY_FOR_RESET:
			switch (gamemode) {
			case AUTO_DROP_MODE:
				startResetAnimation();
			}
			break;

		case IN_RESET_ANIMATION:
			if (grid.isAnyCoinInResetAnimation() == false) {
				initNewGame();
			}
			break;
		}
	}

	public void doMove(int columnIndex) {
		if (gamestate != Game.PLAYING) {
			return;
		}
		
		int[] gridUpdateInfo = gamelogic.doMove(columnIndex);
		switch (gridUpdateInfo[0]) {
		case Gamelogic.INVALID_MOVE:
			return;
			
		case Gamelogic.VALID_MOVE:
			// updated grid
			int rowInd = gridUpdateInfo[1]; 
			int colInd = gridUpdateInfo[2]; 
			int currentPlayer = gridUpdateInfo[3];
			grid.setState(rowInd, colInd, currentPlayer);
			
			// game won or draw?
			if (gamelogic.didGameEndInWin()) {
				gamestate = MATCH_ENDED;
				// increment points
				switch (gamelogic.getPlayerWon()) {
				case Gamelogic.RED:
					redScore++;
					break;

				case Gamelogic.YELLOW:
					yellowScore++;
					break;
				}
				// increment number of games played
				numOfGamesPlayed++;
				// start animation
				grid.startBlinkAnimation(gamelogic.getWinningRowIndices(), gamelogic.getWinningColIndices());
			} else if (gamelogic.didGameEndInDraw()) {
				gamestate = MATCH_ENDED;
				// increment number of games played and number of draws
				numOfGamesPlayed++;
				numOfDraws++;
			}
		}
	}

	public int mouseXToColumnIndex(int mouseX) {
		for (int colInd = 0; colInd < gamelogic.getNumOfColumns(); colInd++) {
			Coin coin = grid.getCoins()[0][colInd];
			double x1 = coin.getTargetX();
			double x2 = coin.getTargetX() + coin.getDiameter();

			if (mouseX > x1 && mouseX < x2) {
				return colInd;
			}
		}
		return -1;
	}

	public boolean isReadyForReset() {
		return gamestate == READY_FOR_RESET;
	}

	/**
	 * This function instantly starts the reset-animation
	 */
	public void startResetAnimation() {
		if (gamestate == IN_RESET_ANIMATION) {
			return;
		}

		gamestate = IN_RESET_ANIMATION;
		grid.startResetAnimation();
	}

	public void toggleAutoDropMode() {
		if (gamemode == Game.AUTO_DROP_MODE) {
			gamemode = Game.TWO_PLAYER_MODE;
		} else {
			gamemode = Game.AUTO_DROP_MODE;
		}
	}

	public void toggleSingleplayerMode() {
		if (gamemode == Game.SINGLEPLAYER_MODE) {
			gamemode = Game.TWO_PLAYER_MODE;
		} else {
			gamemode = Game.SINGLEPLAYER_MODE;
		}
	}

	public int getGamemode() {
		return gamemode;
	}

	public int getCurrentPlayer() {
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
