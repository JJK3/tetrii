package org.jakrabbit.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Tetris extends JPanel implements KeyListener {
	private final Board board;
	private final static int BLOCK_SIZE = 30;

	public Tetris(int width, int height) {
		this.board = new Board(width, height);
		this.setPreferredSize(new Dimension(BLOCK_SIZE * width, BLOCK_SIZE * height));
		this.addKeyListener(this);
		final Tetris self = this;
		Thread pushDown = new Thread() {
			public void run() {
				while (!board.isGameDone()) {
					try {
						sleep(1000);
						board.pushCurrentPieceDown();
						self.repaint();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		};
		pushDown.start();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			board.pushCurrentPieceDown();
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			board.currentPieceRotate();
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			board.currentPieceLeft();
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			board.currentPieceRight();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			while (!board.pushCurrentPieceDown()) {
			}
		}
		this.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponents(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		paintBlocks(board.getCurrentPiece().getBlocks(), g);
		paintBlocks(board.getPlacedBlocks(), g);
        g.setColor(java.awt.Color.BLACK);
        g.setFont(Font.decode("Arial-BOLD-18"));
        g.drawString("Score: " + board.getScore(), (this.getWidth() / 2) - 40, 20);
	}

	private void paintBlocks(List<Block> bs, Graphics g) {
		for (Block b : bs) {
			paintBlock(b, g);
		}
	}

	private void paintBlock(Block b, Graphics g) {
		g.setColor(new Color(b.color.colorHexValue));
		g.fillRect(b.x * BLOCK_SIZE, b.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
		g.setColor(Color.BLACK);
		g.drawRect(b.x * BLOCK_SIZE, b.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
	}

	public static void main(String[] args) {
		final JFrame main = new JFrame("Tetris");
		main.setResizable(false);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setLocationRelativeTo(null);
		Tetris canvas = new Tetris(10, 20);
		main.addKeyListener(canvas);
		main.add(canvas);
		main.pack();
		main.setVisible(true);
	}
}
