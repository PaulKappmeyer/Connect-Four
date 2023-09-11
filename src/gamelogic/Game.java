package gamelogic;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import visuals.Coin;
import visuals.Grid;

public class Game {

	private Grid grid;

	public static final int TWO_PLAYER_MODE = 0;
	public static final int AUTO_DROP_MODE = 1;
	public static final int SINGLEPLAYER_MODE = 2;

	private int gamemode;

	private static final int PLAYING = 0;
	private static final int GAME_ENDED = 1;
	private static final int READY_FOR_RESET = 2;
	private static final int IN_RESET_ANIMATION = 3;

	private int gamestate;

	private int numOfGamesPlayed;
	private int numOfDraws;
	private int redScore;
	private int yellowScore;
	private int currentPlayer;
	private int movesPlayed;

	// used for auto drop mode
	private double timeSinceLastDrop;
	private final double dropTime = 0.39;

	private final int numRows;
	private final int numColumns;
	private final int numNeedForWin;
	
	public Game(int numRows, int numColumns, int numNeedForWin) {
		grid = new Grid(numRows, numColumns, numNeedForWin);
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.numNeedForWin = numNeedForWin;
		gamemode = TWO_PLAYER_MODE;
		gamestate = PLAYING;
		initNewGame();
	}

	/**
	 * This function starts a new game with the current player being red
	 */
	private void initNewGame() {
		grid.initNewGame();
		gamestate = PLAYING;
		movesPlayed = 0;
		currentPlayer = Coin.RED;
	}



	public void draw(Graphics graphics) {
		grid.draw(graphics);

		// draw text
		drawInfoText(graphics);
	}

	private static final int TEXT_Y1 = (int) (0.94 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y2 = (int) (0.96 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y3 = (int) (0.98 * Main.SCREEN_HEIGHT);
	
	private static final String INFO_TEXT2 = "Use mouseclicks or keys 0-9 to drop your tokens. Hit 'Enter' to start a new game. Press 'T' to toggle the automatic drop mode. Press 'S' to toggle the singleplayer mode and play against a (very bad) computer.";
	private static final String INFO_TEXT3 = "Use the arrow keys to change the size of the board. Use the '+' and '-' key to change the number of tokens in a row required for a win."; 
	
	private void drawInfoText(Graphics graphics) {
		graphics.setColor(Color.BLACK);
		graphics.drawString(numRows + "x" + numColumns + "-Grid", 10, 15);
		
		graphics.drawString("Moves played: " + movesPlayed, 10, TEXT_Y1);

		graphics.setColor(Color.RED);
		graphics.drawString("Red score: " + redScore, 150, TEXT_Y1);

		graphics.setColor(Color.ORANGE);
		graphics.drawString("Yellow score: " + yellowScore, 250, TEXT_Y1);

		graphics.setColor(Color.BLACK);
		graphics.drawString("Number of draws: " + numOfDraws, 400, TEXT_Y1);
		graphics.drawString("Number of games played: " + numOfGamesPlayed, 550, TEXT_Y1);
		switch (currentPlayer) {
		case Coin.YELLOW:
			graphics.setColor(Color.ORANGE);
			graphics.drawString("Current Player: Yellow", 750, TEXT_Y1);
			break;

		case Coin.RED:
			graphics.setColor(Color.RED);
			graphics.drawString("Current Player: Red", 750, TEXT_Y1);

		}

		graphics.setColor(Color.BLACK);
		switch (gamemode) {
		case TWO_PLAYER_MODE:
			graphics.drawString("Current gamemode: player vs. player", 900, TEXT_Y1);
			break;

		case AUTO_DROP_MODE:
			graphics.drawString("Current gamemode: automatic drop", 900, TEXT_Y1);
			break;

		case SINGLEPLAYER_MODE:
			graphics.drawString("Current gamemode: player vs. computer", 900, TEXT_Y1);
			break;
		}

		graphics.drawString(INFO_TEXT2, 10, TEXT_Y2);
		graphics.drawString(INFO_TEXT3, 10, TEXT_Y3);
	}

	public void update(double tslf) {
		grid.update(tslf);

		switch (gamestate) {
		case PLAYING:
			switch (gamemode) {
			case TWO_PLAYER_MODE:
				break;

			case AUTO_DROP_MODE:
				updateAutomaticDrop(tslf);
				break;

			case SINGLEPLAYER_MODE:
				if (currentPlayer == Coin.YELLOW) {
					updateAutomaticDrop(tslf);
				}

				break;
			}
			break;

		case GAME_ENDED:
			if (!grid.isAnyCoinInDropAnimation()) {
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
			if (!grid.isAnyCoinInResetAnimation()) {
				initNewGame();
			}
			break;
		}
	}

	private void updateAutomaticDrop(double tslf) {
		timeSinceLastDrop += tslf;
		if (timeSinceLastDrop > dropTime) {
			timeSinceLastDrop -= dropTime;

			dropCoin(new Random().nextInt(numColumns));
		}
	}

	/**
	 * This function tries to drop a coin in a given column.
	 * If successful the number of {@link #movesPlayed} gets increased by one,
	 * the {@link #grid} array a the given position gets changed,
	 * the function {@link #checkForWin()} gets called,
	 * and if the turn does not relate into a victory or draw the function {@link #switchPlayer()} gets called
	 *
	 * @param columnIndex In which column of the array the coin should be dropped.
	 */
	public void dropCoin(int columnIndex) {
		if (gamestate != Game.PLAYING) {
			return;
		}

		if (columnIndex < 0 || columnIndex > numColumns-1) {
			return; // throw new ArrayIndexOutOfBoundsException("index bellow zero or out of allow range:" + (columns-1));
		}

		int freeRowIndex = -1;
		for (int y = numRows - 1; y >= 0; y--) {
			if (grid.getState(columnIndex, y) == Coin.NOT_DROPPED) {
				freeRowIndex = y;
				break;
			}
		}
		if (freeRowIndex == -1) {
			return; // throw new ArrayIndexOutOfBoundsException("No coin can be placed because the column is full");
		}


		// increment number of moves played
		movesPlayed++;
		// updated grid
		grid.setState(columnIndex, freeRowIndex, currentPlayer);

		// check for win
		if (grid.checkForWin(currentPlayer, numNeedForWin)) {
			// increment points
			switch (currentPlayer) {
			case Coin.RED:
				redScore++;
				break;
				
			case Coin.YELLOW:
				yellowScore++;
				break;
			}
			// start blinking animation of 'winning' coins
			grid.setBlinkAnimation(true);
			gamestate = GAME_ENDED;
			numOfGamesPlayed++;
			return;
		}

		// check for draw
		if (movesPlayed == numColumns * numRows) {
			gamestate = GAME_ENDED;
			numOfGamesPlayed++;
			numOfDraws++;
			return;
		}

		// switch current player
		switchPlayer();
	}

	public int mouseXToColumnIndex(int mouseX) {
		for (int i = 0; i < numColumns; i++) {
			Coin coin = grid.getCoins()[i][0];
			double x1 = coin.getTargetX();
			double x2 = coin.getTargetX() + coin.getDiameter();

			if (mouseX > x1 && mouseX < x2) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Switches the current player
	 */
	private void switchPlayer() {
		switch (currentPlayer) {
		case Coin.RED:
			currentPlayer = Coin.YELLOW;
			break;
			
		case Coin.YELLOW:
			currentPlayer = Coin.RED;
			break;
		}
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
		return currentPlayer;
	}
}
