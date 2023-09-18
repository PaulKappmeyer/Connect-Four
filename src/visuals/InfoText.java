package visuals;

import java.awt.Color;
import java.awt.Graphics;

import gamelogic.Game;
import gamelogic.Main;

public class InfoText {

	private static final int TEXT_Y1 = (int) (0.94 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y2 = (int) (0.96 * Main.SCREEN_HEIGHT);
	private static final int TEXT_Y3 = (int) (0.98 * Main.SCREEN_HEIGHT);
	
	private static final String INFO_TEXT2 = "Use mouseclicks or keys 0-9 to drop your tokens. Hit 'Enter' to start a new game. Press 'T' to toggle the automatic drop mode. Press 'S' to toggle the singleplayer mode and play against a (very bad) computer.";
	private static final String INFO_TEXT3 = "Use the arrow keys to change the size of the board. Use the '+' and '-' key to change the number of tokens in a row required for a win. Press 'Z' to undo a move."; 
	
	private Game game;
	
	public InfoText(Game game) {
		this.game = game;
	}
	
	public void draw(Graphics graphics) {
		graphics.setColor(Color.BLACK);
		graphics.drawString(Main.getNumRows() + "x" + Main.getNumColumns() + "-Grid", 10, 15);
		
		graphics.drawString("Moves played: " + game.getNumOfMovesPlayed(), 10, TEXT_Y1);

		graphics.setColor(Color.RED);
		graphics.drawString("Red score: " + game.getRedScore(), 150, TEXT_Y1);

		graphics.setColor(Color.ORANGE);
		graphics.drawString("Yellow score: " + game.getYellowScore(), 250, TEXT_Y1);

		graphics.setColor(Color.BLACK);
		graphics.drawString("Number of draws: " + game.getNumOfDraws(), 400, TEXT_Y1);
		graphics.drawString("Number of games played: " + game.getNumOfGamesPlayed(), 550, TEXT_Y1);
		switch (game.getCurrentPlayer()) {
		case NOT_DROPPED:
			break;
		
		case YELLOW:
			graphics.setColor(Color.ORANGE);
			graphics.drawString("Current Player: Yellow", 750, TEXT_Y1);
			break;

		case RED:
			graphics.setColor(Color.RED);
			graphics.drawString("Current Player: Red", 750, TEXT_Y1);

		}

		graphics.setColor(Color.BLACK);
		switch (game.getGamemode()) {
		case PLAYER_VS_PLAYER:
			graphics.drawString("Current gamemode: player vs. player", 900, TEXT_Y1);
			break;

		case AUTO_DROP:
			graphics.drawString("Current gamemode: automatic drop", 900, TEXT_Y1);
			break;

		case PLAYER_VS_COMPUTER:
			graphics.drawString("Current gamemode: player vs. computer", 900, TEXT_Y1);
			break;
		}

		graphics.drawString(INFO_TEXT2, 10, TEXT_Y2);
		graphics.drawString(INFO_TEXT3, 10, TEXT_Y3);
	}
	
}
