package gamelogic;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import visuals.Coin;

public class InputManager implements KeyListener, MouseListener{

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		Game game = Main.getGame();
		
		switch (game.getGamemode()) {
		case Game.TWO_PLAYER_MODE:
			checkDroppingKeys(keyCode);
			break;

		case Game.SINGLEPLAYER_MODE:
			if (game.getCurrentPlayer() == Coin.RED) {
				checkDroppingKeys(keyCode);
			}
			break;
		}

		switch (keyCode) {
		case KeyEvent.VK_ENTER:
			game.startResetAnimation();
			break;

		case KeyEvent.VK_ESCAPE:
			System.exit(1);
			break;

		case KeyEvent.VK_T:
			game.toggleAutoDropMode();
			break;

		case KeyEvent.VK_S:
			game.toggleSingleplayerMode();
			break;
			
		case KeyEvent.VK_UP:
			Main.changeBoardSize(Main.getNumRows() + 1, Main.getNumColumns());
			break;
		case KeyEvent.VK_DOWN:
			Main.changeBoardSize(Main.getNumRows() - 1, Main.getNumColumns());
			break;
		case KeyEvent.VK_LEFT:
			Main.changeBoardSize(Main.getNumRows(), Main.getNumColumns() - 1);
			break;
		case KeyEvent.VK_RIGHT:
			Main.changeBoardSize(Main.getNumRows(), Main.getNumColumns() + 1);
			break;
			
		case KeyEvent.VK_PLUS:
			Main.changeNumNeedForWin(Main.getNumNeedForWin() + 1);
			break;
		case KeyEvent.VK_MINUS:
			Main.changeNumNeedForWin(Main.getNumNeedForWin() - 1);
			break;

		default:
			break;
		}
	}

	private void checkDroppingKeys(int keyCode) {
		Game game = Main.getGame();
		
		switch (keyCode) {
		case KeyEvent.VK_1:
			game.dropCoin(0);
			break;
		case KeyEvent.VK_2:
			game.dropCoin(1);
			break;
		case KeyEvent.VK_3:
			game.dropCoin(2);
			break;
		case KeyEvent.VK_4:
			game.dropCoin(3);
			break;
		case KeyEvent.VK_5:
			game.dropCoin(4);
			break;
		case KeyEvent.VK_6:
			game.dropCoin(5);
			break;
		case KeyEvent.VK_7:
			game.dropCoin(6);
			break;
		case KeyEvent.VK_8:
			game.dropCoin(7);
			break;
		case KeyEvent.VK_9:
			game.dropCoin(8);
			break;
		case KeyEvent.VK_0:
			game.dropCoin(9);
			break;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	//-------------------------------------------------------------------------------------------------------------
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Game game = Main.getGame();
		
		if (game.isReadyForReset()) {
			game.startResetAnimation();
			return;
		}
		int mouseX = e.getX();

		switch (game.getGamemode()) {
		case Game.TWO_PLAYER_MODE:
			game.dropCoin(game.mouseXToColumnIndex(mouseX));
			break;

		case Game.SINGLEPLAYER_MODE:
			if (game.getCurrentPlayer() == Coin.RED) {
				game.dropCoin(game.mouseXToColumnIndex(mouseX));
			}
		}
	}
}
