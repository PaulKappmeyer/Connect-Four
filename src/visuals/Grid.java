package visuals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import gamelogic.Main;

public class Grid {

	private static final boolean SHOW_ANIMATIONS = true;

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

	private boolean showBlinkAnimation;


	public Grid(int numRows, int numColumns, int numNeedForWin) {
		this.numRows = numRows;
		this.numColumns = numColumns;
		this.numNeedForWin = numNeedForWin;


		double boundingBoxSize = Math.min(WINDOW_HEIGHT_OF_GRID / (double) numRows, Main.SCREEN_WIDTH / (double) numColumns);
		this.coinDiameter = 0.95 * boundingBoxSize;
		this.xSpacing = 0.05 * boundingBoxSize;
		this.ySpacing = 0.04 * boundingBoxSize;
		this.xOffset = (Main.SCREEN_WIDTH - numColumns * (coinDiameter + xSpacing) + xSpacing) / 2.;
		this.yOffset = (0.9 * Main.SCREEN_HEIGHT - numRows * (coinDiameter + ySpacing) + ySpacing) / 2.;
		this.DROP_Y = yOffset - coinDiameter;

		this.backgroundArea = new Area(new Rectangle.Double(0, DROP_Y + coinDiameter/2, Main.SCREEN_WIDTH, numRows * (coinDiameter + xSpacing) + coinDiameter/2));
		this.coins = new Coin[numRows][numColumns];
		for (int colInd = 0; colInd < numColumns; colInd++) {
			for (int rowInd = 0; rowInd < numRows; rowInd++) {
				double xPos = colInd * (coinDiameter + xSpacing) + xOffset;
				double yPos = rowInd * (coinDiameter + ySpacing) + yOffset;
				coins[rowInd][colInd] = new Coin(xPos, yPos, coinDiameter, this);
				backgroundArea.subtract(new Area(new Ellipse2D.Double(xPos, yPos, coinDiameter, coinDiameter)));
			}
		}
	}

	public void initNewGame() {
		for (Coin[] coinRow : coins) {
			for (Coin coin : coinRow) {
				coin.initNewGame();
			}
		}
	}

	public void draw(Graphics graphics) {		
		// draw coins and cut out spaces
		for (Coin[] coinRow : coins) {
			for (Coin coin : coinRow) {
				coin.draw(graphics);
			}
		}

		// Draw Background
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.fill(backgroundArea);
	}

	public void update(double tslf) {
		for (int colInd = 0; colInd < numColumns; colInd++) {
			for (int rowInd = 0; rowInd < numRows; rowInd++) {
				Coin coin = coins[rowInd][colInd];
				coin.update(tslf);

				// check collision
				if (rowInd >= 1) {
					Coin above = coins[rowInd-1][colInd];

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
		if (coin1.isInDropAnimation() == false || coin2.isInDropAnimation() == false) {
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
		if (SHOW_ANIMATIONS) {
			for (Coin[] coinRow : coins) {
				for (Coin coin : coinRow) {
					coin.startResetAnimation();
				}
			}			
		}
		stopBlinkAnimation();
	}

	/**
	 * This function calculates which coins need the blink after one player has won
	 * @param blink true if the coins should blink, false if they should not blink
	 */
	public void startBlinkAnimation(int[] rowIndices, int[] colIndices) {
		showBlinkAnimation = true;
		for (int i = 0; i < numNeedForWin; i++) {
			coins[rowIndices[i]][colIndices[i]].setBlink(true);
		}
	}

	public void stopBlinkAnimation() {
		for (Coin[] coinRow : coins) {
			for (Coin coin : coinRow) {
				coin.setBlink(false);
			}
		}
	}

	/**
	 * This function indicates if any coin is in the drop-animation
	 * @return true if any coin is in drop-animation, false if not
	 */
	public boolean isAnyCoinInDropAnimation() {
		for (Coin[] coinRow : coins) {
			for (Coin coin : coinRow) {
				if (coin.isInDropAnimation()) {
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
		for (Coin[] coinRow : coins) {
			for (Coin coin : coinRow) {
				if (coin.isInResetAnimation()) {
					return true;
				}
			}
		}
		return false;
	}

	public int getState(int rowInd, int colInd) {
		return coins[rowInd][colInd].getState();
	}

	public void setState(int rowInd, int colInd, int state) {
		coins[rowInd][colInd].setState(state);

		if (SHOW_ANIMATIONS) {
			coins[rowInd][colInd].startDropAnimation();
		}
	}

	public Coin[][] getCoins() {
		return coins;
	}
}