/*
 * 
 */
package gameengine.graphics;

import java.awt.image.BufferStrategy;
import java.awt.Insets;
import java.awt.Graphics;

import javax.swing.JFrame;

/**
 * 
 * @author Paul Kappmeyer & Daniel Lucarz
 * 
 */


@SuppressWarnings("serial")
public class Window extends JFrame {
	private int insetX;
	private int insetY;
	private BufferStrategy strat;

	public Window(String title, int width, int height) {
		this.setTitle(title);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setUndecorated(false);
		this.setVisible(true);
		this.setResizable(false);

		createBufferStrategy(2);
		strat = getBufferStrategy();

		Insets i = getInsets();
		insetX = i.left;
		insetY = i.top;

		this.setSize(width + insetX + i.right, height + insetY + i.bottom);
		this.setLocationRelativeTo(null);
	}

	public int getInsetX() {
		return insetX;
	}

	public int getInsetY() {
		return insetY;
	}

	public Graphics beginDrawing() {
		Graphics graphics = strat.getDrawGraphics();
		graphics.translate(insetX, insetY);
		return graphics;
	}

	public void endDrawing(Graphics g){
		g.dispose();
		strat.show();
	}
}