package gamelogic;

import java.util.Random;

public abstract class Bot {

	private static final Random rand = new Random();
	
	public abstract int getNextMove(Gamelogic position);
	
	protected int getPossibleRandomMove(Gamelogic position) {
		int columnIndex;
		do {
			columnIndex = (int) rand.nextGaussian(position.getNumOfColumns()/2, 1);
		} while (position.isPossibleMove(columnIndex) == false);
		return columnIndex;
	}
}
