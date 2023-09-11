package visuals;

import java.awt.Color;
import java.awt.Graphics;

import gamelogic.Main;

public class Coin {

	public static final int NOT_DROPPED = 0;
	public static final int RED = 1;
	public static final int YELLOW = -1;
	
	private static final int NO_ANIMATION = -1;
	private static final int DROP_ANIMATION = 0;
	private static final int RESET_FALL_ANIMATION = 2;

	private int animationState;
	
	// used to indicate which player dropped the coin / if it is was drop
	private int coinstate;
	private Color color;

	// target position on screen
	private final double targetX;
	private final double targetY;
	private final double diameter;
	private double currentY;

	// drop animation
	private static final double GRAVITY = 3000;
	private static final double ENERGY_LOSS_ON_BOUNCE = 0.59;
	private static final double STOP_ANIMATION_SPEED = 150;
	private double speed;

	// blink animation after a player won
	private static final double BLINK_SWITCH_TIME = 0.125f;
	private static double aktBlinkTime;
	private static boolean swapBlink;
	private boolean blinking;
	
	private Grid grid;

	public Coin(double targetX, double targetY, double diameter, Grid grid) {
		this.coinstate = NOT_DROPPED;
		this.targetX = targetX;
		this.targetY = targetY;
		this.diameter = diameter;
		this.currentY = targetY;
		this.grid = grid;
		this.animationState = NO_ANIMATION;
	}

	public void initNewGame() {
		coinstate = NOT_DROPPED;
		animationState = NO_ANIMATION;
		currentY = targetY;
	}

	public void draw(Graphics graphics) {
		if (coinstate == NOT_DROPPED) {
			return;
		}

		// for blink animation
		graphics.setColor(color);
		if (blinking) {
			if (swapBlink) {
				graphics.setColor(Color.WHITE);
			}
		}

		// fill coin
		graphics.fillOval((int) targetX , (int) currentY, (int) diameter, (int) diameter);

		// draw outline
		graphics.setColor(Color.BLACK);
		graphics.drawOval((int) targetX, (int) currentY, (int) diameter, (int) diameter);
	}

	public void update(double tslf) {
		switch (animationState) {
		case DROP_ANIMATION:
			// update position and speed
			move(tslf);

			// do bounces
			if (currentY > targetY) {
				currentY = targetY;
				speed *= (-1. + ENERGY_LOSS_ON_BOUNCE);
				
				// very slow? -> stop drop animation
				if (Math.abs(speed) <= STOP_ANIMATION_SPEED) {
					stopDropAnimation();
				}
			}

			break;

		case RESET_FALL_ANIMATION:
			// update position and speed
			move(tslf);

			// check if left the screen
			if (currentY > Main.SCREEN_HEIGHT ) {
				stopDropAnimation();
			}
			break;
		}
	}

	private void move(double tslf) {
		currentY += speed * tslf;
		speed += GRAVITY * tslf;
	}

	public void setState(int player) {
		coinstate = player;
		switch (coinstate) {
		case RED:
			color = Color.RED;
			break;
		case YELLOW:
			color = Color.YELLOW;
			break;
		default:
			break;
		}
	}

	public void startDropAnimation() {
		animationState = DROP_ANIMATION;
		currentY = grid.DROP_Y;
		speed = 0;
	}

	private void stopDropAnimation() {
		animationState = NO_ANIMATION;
	}

	/**
	 * This function updates the blink of the coins when a player has won. Static because the should all have the same timing
	 * @param tslf The time since the last frame
	 */
	public static void updateBlink(double tslf) {
		aktBlinkTime += tslf;
		if (aktBlinkTime >= BLINK_SWITCH_TIME) {
			aktBlinkTime -= BLINK_SWITCH_TIME;
			swapBlink = !swapBlink;
		}
	}

	/**
	 * This function starts the reset-animation.
	 */
	public void startResetAnimation() {
		animationState = RESET_FALL_ANIMATION;
		speed = 0;
	}


	/**
	 * This function sets the blink. The coins blink when the where the coins relating into a victory
	 * @param blink True if this coin should blink, false if not
	 */
	public void setBlink(boolean blink) {
		this.blinking = blink;
	}


	public boolean isInDropAnimation() {
		return animationState == DROP_ANIMATION;
	}

	public boolean isInResetAnimation() {
		return animationState == RESET_FALL_ANIMATION;
	}

	public int getState() {
		return coinstate;
	}

	public double getTargetX() {
		return targetX;
	}

	public double getCurrentY() {
		return currentY;
	}

	public void setCurrentY(double currentY) {
		this.currentY = currentY;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getDiameter() {
		return diameter;
	}
}
