package gamelogic;

import java.awt.Color;
import java.awt.Graphics;

import gameengine.GameBase;

public class Main extends GameBase {

	public static final int SCREEN_WIDTH = 1150;
	public static final int SCREEN_HEIGHT = 856;

	public static final int STANDARD_NUM_ROWS = 6;
	public static final int STANDARD_NUM_COLUMNS = 7;
	public static final int STANDARD_NUM_NEED_FOR_WIN = 4;
	
	private static final String[] WINDOW_TITLES = {"Eins Gewinnt!", "Zwei Gewinnt!", "Drei Gewinnt!", "Vier Gewinnt", "Fünf Gewinnt!", "Sechs Gewinnt!"};
	
	private static int numRows;
	private static int numColumns;
	private static int numNeedForWin;
	
	private static boolean gameSettingsChanged;

	
	private static Game game;

	public static void main(String[] args) {
		Main main = new Main();
		main.start(WINDOW_TITLES[STANDARD_NUM_NEED_FOR_WIN - 1], SCREEN_WIDTH, SCREEN_HEIGHT);
	}

	@Override
	public void init() {
		numRows = STANDARD_NUM_ROWS;
		numColumns = STANDARD_NUM_COLUMNS;
		numNeedForWin = STANDARD_NUM_NEED_FOR_WIN;
		game = new Game(numRows, numColumns, numNeedForWin);
		
		// Adding inputManagers to window
		window.addKeyListener(new InputManager());
		window.addMouseListener(new InputManager());
	}

	public static void changeBoardSize(int newNumRows, int newNumColumns) {
		numRows = newNumRows;
		numColumns = newNumColumns;
		if (numRows < 1) {
			numRows = 1;
		} else if (numRows > 11) {
			numRows = 11;
		}
		if (numColumns < 1) {
			numColumns = 1;
		} else if (numColumns > 11) {
			numColumns = 11;
		}
		gameSettingsChanged = true;
	}
	
	public static void changeNumNeedForWin(int newNumNeedForWin) {
		numNeedForWin = newNumNeedForWin;
		if (numNeedForWin < 1) {
			numNeedForWin = 1;
		} else if (numNeedForWin > 6) {
			numNeedForWin = 6;
		}
		gameSettingsChanged = true;
	}
	
	
	@Override
	public void update(double tslf) {
		if (gameSettingsChanged) {
			game = new Game(numRows, numColumns, numNeedForWin);
			window.setTitle(WINDOW_TITLES[numNeedForWin - 1]);
			gameSettingsChanged = false;
		}
		
		game.update(tslf);
	}

	@Override
	public void draw(Graphics graphics) {
		// Draw Background
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);

		game.draw(graphics);
	}

	public static Game getGame() {
		if (game == null) {
			System.err.println("TODO");
		}
		return game;
	}
	
	public static int getNumRows() {
		return numRows;
	}
	
	public static int getNumColumns() {
		return numColumns;
	}
	
	public static int getNumNeedForWin() {
		return numNeedForWin;
	}
}
