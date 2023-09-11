package visuals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import gamelogic.Main;

public class Grid {

	private static final double WINDOW_HEIGHT_OF_GRID = 0.9 * Main.SCREEN_HEIGHT;
	
	public final double DROP_Y;
	
	private final int numRows;
	private final int numColumns;
	private final int numNeedForWin;
	
	private final double coinDiameter;
	private final double xSpacing;
	private final double ySpacing;
	private final double xOffset;
	private final double yOffset;

	private final Area backgroundArea;
	
	private final Coin[][] coins;
	private final int[] winningIndicesX;
	private final int[] winningIndicesY;

	private boolean showBlinkAnimation;
	
	
	public Grid(int numRows, int numColumns, int numNeedForWin) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.numNeedForWin = numNeedForWin;
		this.winningIndicesX = new int[numNeedForWin];
		this.winningIndicesY = new int[numNeedForWin];
		
		double boundingBoxSize = Math.min(WINDOW_HEIGHT_OF_GRID / (double) numRows, Main.SCREEN_WIDTH / (double) numColumns);
		this.coinDiameter = 0.95 * boundingBoxSize;
		this.xSpacing = 0.05 * boundingBoxSize;
		this.ySpacing = 0.04 * boundingBoxSize;
		this.xOffset = (Main.SCREEN_WIDTH - numColumns * (coinDiameter + xSpacing) + xSpacing) / 2.;
		this.yOffset = (0.9 * Main.SCREEN_HEIGHT - numRows * (coinDiameter + ySpacing) + ySpacing) / 2.;
		this.DROP_Y = yOffset - coinDiameter;

		this.backgroundArea = new Area(new Rectangle.Double(0, DROP_Y + coinDiameter/2, Main.SCREEN_WIDTH, numRows * (coinDiameter + xSpacing) + coinDiameter/2));
		this.coins = new Coin[numColumns][numRows];
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				double xPos = i * (coinDiameter + xSpacing) + xOffset;
				double yPos = j * (coinDiameter + ySpacing) + yOffset;
				coins[i][j] = new Coin(xPos, yPos, coinDiameter, this);
				backgroundArea.subtract(new Area(new Ellipse2D.Double(xPos, yPos, coinDiameter, coinDiameter)));
			}
		}
	}

	public void initNewGame() {
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				coins[i][j].initNewGame();
			}
		}
	}

	public void draw(Graphics graphics) {		
		// draw coins and cut out spaces
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				Coin coin = coins[i][j];
				coin.draw(graphics);
			}
		}

		// Draw Background
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fill(backgroundArea);
	}

	public void update(double tslf) {
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				Coin coin = coins[i][j];
				coin.update(tslf);

				// check collision
				if (j >= 1) {
					Coin above = coins[i][j-1];
					
					checkCollision(coin, above);
				}
			}
		}
		if (showBlinkAnimation) {
			Coin.updateBlink(tslf);
		}
	}

	private void checkCollision(Coin coin1, Coin coin2) {
		// early exit: both coins have to be falling
		if (!coin1.isInDropAnimation() || !coin2.isInDropAnimation()) {
			return;
		}

		double coin1CurrentY = coin1.getCurrentY();
		double coin2CurrentY = coin2.getCurrentY();
		double distanceY = coin1CurrentY - coin2CurrentY;
		// early exit: the circles need to intersect
		if (distanceY > coinDiameter) {
			return;
		}

		double coin1Speed = coin1.getSpeed();
		double coin2Speed = coin2.getSpeed();
		double velDiffY = coin2Speed - coin1Speed;

		// going towards each other? -> bounce them
		double dot = distanceY * velDiffY;
		if (dot > 0) {
			coin1.setSpeed(coin2Speed);
			coin2.setSpeed(coin1Speed);
		}
	}
	
	/**
	 * This function starts the reset-animation with start the fall of the coins and stop the blinking
	 */
	public void startResetAnimation() {
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				coins[i][j].startResetAnimation();
			}
		}
		setBlinkAnimation(false);
	}


	/**
	 * Checks if the current turn relates into a win for the current player
	 * @param player the color to check a win for, often it is the color of the current turn
	 * @return true if the current player has won, false if not
	 */
	public boolean checkForWin(int player, int numNeedForWin) {
		// Check for horizontal row
		for (int startY = 0; startY < numRows; startY++) {
			outer: for (int startX = 0; startX <= numColumns - numNeedForWin; startX++) {
				// clear arrays
				clearWinningIndicesArray();
				
				for (int xOff = 0; xOff < numNeedForWin; xOff++) {
					int xInd = startX + xOff;
					if (coins[xInd][startY].getState() != player) {
						continue outer;
					}
					
					winningIndicesX[xOff] = xInd;
					winningIndicesY[xOff] = startY;
				}
				
				return true;
			}
		}
		
		// Check for vertical row
		for (int startX = 0; startX < numColumns; startX++) {
			outer: for (int startY = 0; startY <= numRows - numNeedForWin; startY++) {
				// clear arrays
				clearWinningIndicesArray();
				
				for (int yOff = 0; yOff < numNeedForWin; yOff++) {
					int yInd = startY + yOff;
					if (coins[startX][yInd].getState() != player) {
						continue outer;
					}
					
					winningIndicesX[yOff] = startX;
					winningIndicesY[yOff] = yInd;
				}
				return true;
			}
		}
		
		// Check for diagonal row: down and right
		for (int startX = 0; startX <= numColumns - numNeedForWin; startX++) {
			outer: for (int startY = 0; startY <= numRows - numNeedForWin; startY++) {
				// clear arrays
				clearWinningIndicesArray();
				
				for (int off = 0; off < numNeedForWin; off++) {
					int xInd = startX + off;
					int yInd = startY + off;
					if (coins[xInd][yInd].getState() != player) {
						continue outer;
					}
					
					winningIndicesX[off] = xInd;
					winningIndicesY[off] = yInd;
				}
				return true;
			}
		}
		
		// Check for diagonal row: up and right
		for (int startX = 0; startX <= numColumns - numNeedForWin; startX++) {
			outer: for (int startY = 0; startY <= numRows - numNeedForWin; startY++) {
				// clear arrays
				clearWinningIndicesArray();
				
				for (int off = 0; off < numNeedForWin; off++) {
					int xInd = startX + off;
					int yInd = startY + (numNeedForWin - 1) - off;
					if (coins[xInd][yInd].getState() != player) {
						continue outer;
					}
					
					winningIndicesX[off] = xInd;
					winningIndicesY[off] = yInd;
				}
				return true;
			}
		}
		
		return false;
	}
	
	private void clearWinningIndicesArray() {
		for (int i = 0; i < numNeedForWin; i++) {
			winningIndicesX[i] = -1;
			winningIndicesY[i] = -1;
		}
	}

	/**
	 * This function calculates which coins need the blink after one player has won
	 * @param blink true if the coins should blink, false if they should not blink
	 */
	public void setBlinkAnimation(boolean blink) {
		this.showBlinkAnimation = blink;
		for (int i = 0; i < numNeedForWin; i++) {
			int xInd = winningIndicesX[i];
			int yInd = winningIndicesY[i];
			
			if (xInd == -1 || yInd == -1) {
				continue;
			}

			coins[xInd][yInd].setBlink(blink);
		}
	}

	/**
	 * This function indicates if any coin is in the drop-animation
	 * @return true if any coin is in drop-animation, false if not
	 */
	public boolean isAnyCoinInDropAnimation() {
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				if (coins[i][j].isInDropAnimation()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This function indicates if any coin is in the reset-animation
	 * @return true if any coin is in reset-animation, false if not
	 */
	public boolean isAnyCoinInResetAnimation() {
		for (int i = 0; i < numColumns; i++) {
			for (int j = 0; j < numRows; j++) {
				if (coins[i][j].isInResetAnimation()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getState(int x, int y) {
		return coins[x][y].getState();
	}

	public void setState(int x, int y, int state) {
		coins[x][y].setState(state);
		coins[x][y].startDropAnimation();
	}

	public Coin[][] getCoins() {
		return coins;
	}
}