package gamelogic;

import java.awt.Graphics;
import java.util.Random;

import visuals.Coin;
import visuals.Grid;
import visuals.InfoText;

public class Game {
	
	private Grid grid;

	public static final int TWO_PLAYER_MODE = 0;
	public static final int AUTO_DROP_MODE = 1;
	public static final int SINGLEPLAYER_MODE = 2;

	private int gamemode;

	private static final int PLAYING = 0;
	private static final int MATCH_ENDED = 1;
	private static final int READY_FOR_RESET = 2;
	private static final int IN_RESET_ANIMATION = 3;

	private int gamestate;

	// used for auto drop mode
	private double timeSinceLastDrop;
	private final double dropTime = 0.1;
	
	private Gamelogic gamelogic;
	
	private InfoText infoText;
	
	public Game(int numRows, int numColumns, int numNeedForWin) {
		gamelogic = new Gamelogic(numRows, numColumns, numNeedForWin);
		gamemode = TWO_PLAYER_MODE;
		gamestate = PLAYING;
		
		grid = new Grid(numRows, numColumns, numNeedForWin);
		
		infoText = new InfoText(this);
		
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
				updateAutomaticDrop(tslf);
				break;

			case SINGLEPLAYER_MODE:
				if (gamelogic.getCurrentPlayer() == Gamelogic.YELLOW) {
					updateAutomaticDrop(tslf);
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

	private static final Random rand = new Random();
	
	private void updateAutomaticDrop(double tslf) {
		timeSinceLastDrop += tslf;
		if (timeSinceLastDrop > dropTime) {
			timeSinceLastDrop -= dropTime;

			int columnIndex = -1; 
			while (gamelogic.isPossibleMove(columnIndex) == false) {
				columnIndex = rand.nextInt(gamelogic.getNumOfColumns());
			}
			dropCoin(columnIndex);
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

		int[] returnMsg = gamelogic.doMove(columnIndex);
		switch (returnMsg[0]) {
		case Gamelogic.INVALID_MOVE:
			return;
			
		case Gamelogic.VALID_MOVE:
			// updated grid
			int rowInd = returnMsg[1]; 
			int colInd = returnMsg[2]; 
			int currentPlayer = returnMsg[3];
			grid.setState(rowInd, colInd, currentPlayer);
			
			// game won or draw?
			if (gamelogic.isGameFinished()) {
				gamestate = MATCH_ENDED;
				
				
				// draw?
				if (gamelogic.isGameFinishedInDraw()) {
					return;
				} else {
					grid.startBlinkAnimation(gamelogic.getWinningRowIndices(), gamelogic.getWinningColIndices());
				}
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
	
	public Gamelogic getGamelogic() {
		return gamelogic;
	}
}
