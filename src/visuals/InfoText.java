package visuals;

import java.awt.Color;
import java.awt.Graphics;

import gamelogic.Game;
import gamelogic.Gamelogic;
import gamelogic.Main;

public class InfoText {

	private static final int TEXT_Y1 = (int) (0.94 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y2 = (int) (0.96 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y3 = (int) (0.98 * Main.SCREEN_HEIGHT);
	
	private static final String INFO_TEXT2 = "Use mouseclicks or keys 0-9 to drop your tokens. Hit 'Enter' to start a new game. Press 'T' to toggle the automatic drop mode. Press 'S' to toggle the singleplayer mode and play against a (very bad) computer.";
	private static final String INFO_TEXT3 = "Use the arrow keys to change the size of the board. Use the '+' and '-' key to change the number of tokens in a row required for a win."; 
	
	private Game game;
	private Gamelogic gamelogic;
	
	public InfoText(Game game) {
		this.game = game;
		this.gamelogic = game.getGamelogic();
	}
	
	public void draw(Graphics graphics) {
		graphics.setColor(Color.BLACK);
		graphics.drawString(Main.getNumRows() + "x" + Main.getNumColumns() + "-Grid", 10, 15);
		
		graphics.drawString("Moves played: " + gamelogic.getNumOfMovesPlayed(), 10, TEXT_Y1);

		graphics.setColor(Color.RED);
		graphics.drawString("Red score: " + gamelogic.getRedScore(), 150, TEXT_Y1);

		graphics.setColor(Color.ORANGE);
		graphics.drawString("Yellow score: " + gamelogic.getYellowScore(), 250, TEXT_Y1);

		graphics.setColor(Color.BLACK);
		graphics.drawString("Number of draws: " + gamelogic.getNumOfDraws(), 400, TEXT_Y1);
		graphics.drawString("Number of games played: " + gamelogic.getNumOfGamesPlayed(), 550, TEXT_Y1);
		switch (gamelogic.getCurrentPlayer()) {
		case Gamelogic.YELLOW:
			graphics.setColor(Color.ORANGE);
			graphics.drawString("Current Player: Yellow", 750, TEXT_Y1);
			break;

		case Gamelogic.RED:
			graphics.setColor(Color.RED);
			graphics.drawString("Current Player: Red", 750, TEXT_Y1);

		}

		graphics.setColor(Color.BLACK);
		switch (game.getGamemode()) {
		case Game.TWO_PLAYER_MODE:
			graphics.drawString("Current gamemode: player vs. player", 900, TEXT_Y1);
			break;

		case Game.AUTO_DROP_MODE:
			graphics.drawString("Current gamemode: automatic drop", 900, TEXT_Y1);
			break;

		case Game.SINGLEPLAYER_MODE:
			graphics.drawString("Current gamemode: player vs. computer", 900, TEXT_Y1);
			break;
		}

		graphics.drawString(INFO_TEXT2, 10, TEXT_Y2);
		graphics.drawString(INFO_TEXT3, 10, TEXT_Y3);
	}
	
}
