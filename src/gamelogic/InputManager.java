package gamelogic;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import gamelogic.Gamelogic.Boardstate;

public class InputManager implements KeyListener, MouseListener{

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();

		Game game = Main.getGame();
		
		switch (game.getGamemode()) {
		case PLAYER_VS_PLAYER:
			checkDroppingKeys(keyCode);
			break;

		case PLAYER_VS_COMPUTER:
			if (game.getCurrentPlayer() == Gamelogic.Boardstate.RED) {
				checkDroppingKeys(keyCode);
			}
			break;
		
		default:
			break;
		}

		switch (keyCode) {
		case KeyEvent.VK_ENTER:
			game.startResetAnimation();
			break;

		case KeyEvent.VK_ESCAPE:
			System.exit(1);
			break;

		case KeyEvent.VK_Z:
			switch (game.getGamemode()) {
			case PLAYER_VS_PLAYER:
				game.undoLastMove();
				break;
				
			case PLAYER_VS_COMPUTER:
				// if playing against the computer undo two moves, so red is playing again
				if (game.getCurrentPlayer() == Boardstate.RED) {
					game.undoLastMove();
					game.undoLastMove();
				}
				break;
				
			case AUTO_DROP:
				break;
			}
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
			game.doMove(0);
			break;
		case KeyEvent.VK_2:
			game.doMove(1);
			break;
		case KeyEvent.VK_3:
			game.doMove(2);
			break;
		case KeyEvent.VK_4:
			game.doMove(3);
			break;
		case KeyEvent.VK_5:
			game.doMove(4);
			break;
		case KeyEvent.VK_6:
			game.doMove(5);
			break;
		case KeyEvent.VK_7:
			game.doMove(6);
			break;
		case KeyEvent.VK_8:
			game.doMove(7);
			break;
		case KeyEvent.VK_9:
			game.doMove(8);
			break;
		case KeyEvent.VK_0:
			game.doMove(9);
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
		case PLAYER_VS_PLAYER:
			game.doMove(game.mouseXToColumnIndex(mouseX));
			break;

		case PLAYER_VS_COMPUTER:
			if (game.getCurrentPlayer() == Gamelogic.Boardstate.RED) {
				game.doMove(game.mouseXToColumnIndex(mouseX));
			}
			break;
		
		default:
			break;
		}
	}
}
