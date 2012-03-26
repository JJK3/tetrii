package org.jakrabbit.tetris;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class Board {
	private List<Block> placedBlocks = new ArrayList<Block>();
	private BlockGroup currentPiece;
	private final int width, height;
	private int score = 0;

	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		currentPiece = BlockGroup.newRandom(width / 2, 1);
	}

	public int getScore() {
		return score;
	}

	/**
	 * Find the block at the given coordinates or null if none exist.
	 */
	public Block blockAt(final int x, final int y) {
		return Iterables.find(placedBlocks, new Predicate<Block>() {
			public boolean apply(Block b) {
				return b.x == x && b.y == y;
			};
		}, null);
	}

	/**
	 * Gets the list of blocks of the given row.
	 * 
	 * @param y
	 *            The row number (0 index)
	 * @return The resulting list has size = this.width and null values where blocks don't exist.
	 */
	public List<Block> getRow(int y) {
		List<Block> result = new ArrayList<Block>();
		for (int i = 0; i < width; i++) {
			result.add(blockAt(i, y));
		}
		return result;
	}

	/** Is the given row complete? Meaning, is completely full of blocks? */
	public boolean isRowComplete(int y) {
		return !getRow(y).contains(null);
	}

	/**
	 * Find all complete rows.
	 * 
	 * @return The List of y-indexes of the completed rows.
	 */
	public List<Integer> findCompletedRows() {
		List<Integer> result = new ArrayList<Integer>();
		for (int y = 0; y < height; y++) {
			if (isRowComplete(y)) {
				result.add(y);
			}
		}
		return result;
	}

	/** Is the given block within bounds? */
	public boolean isBlockValid(Block b) {
		return b.x >= 0 && b.x < width && b.y >= 0 && b.y < height && blockAt(b.x, b.y) == null;
	}

	/** Is the entire given block group within bounds? */
	public boolean isBlockGroupValid(BlockGroup bg) {
		return Iterables.all(bg.getBlocks(), new Predicate<Block>() {
			@Override
			public boolean apply(Block b) {
				return isBlockValid(b);
			}
		});
	}

	public void placePiece(BlockGroup bg) {
		if (!isBlockGroupValid(bg)) {
			throw new IllegalArgumentException("Cannot place an invalid block group. " + bg);
		}
		placedBlocks.addAll(bg.getBlocks());
	}

	public void removeRow(final int y) {
		placedBlocks.removeAll(getRow(y));
		// push blocks above y, down
		placedBlocks = Lists.transform(placedBlocks, new Function<Block, Block>() {
			@Override
			public Block apply(Block old) {
				return (old.y < y) ? old.down() : old;
			}
		});
	}

	public boolean isPieceOnBottom(BlockGroup bg) {
		return isBlockGroupValid(bg.down());
	}

	public boolean pushCurrentPieceDown() {
		if (isPieceOnBottom(currentPiece)) {
			placePiece(currentPiece);
			List<Integer> completeRows = findCompletedRows();
			for (int y : completeRows) {
				removeRow(y);
			}
			if (completeRows.size() == 1) {
				score += 10;
			} else if (completeRows.size() == 2) {
				score += 25;
			} else if (completeRows.size() == 3) {
				score += 40;
			} else if (completeRows.size() == 4) {
				score += 55;
			}
			currentPiece = BlockGroup.newRandom(width / 2, 1);
			return true;
		} else {
			currentPiece = currentPiece.down();
		}
		return false;
	}
}
