package org.jakrabbit.tetris;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class BlockGroup {
	private final Block center;
	private final List<Block> blocks;

	public BlockGroup(Block center, List<Block> blocks) {
		this.center = center;
		this.blocks = blocks;
	}

	public Block getCenter() {
		return center;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	/** Create a new group, shifted down. */
	public BlockGroup down() {
		return new BlockGroup(center.down(), Lists.transform(this.blocks, Block.down));
	}

	/** Create a new group, shifted left. */
	public BlockGroup left() {
		return new BlockGroup(center.left(), Lists.transform(this.blocks, Block.left));
	}

	/** Create a new group, shifted right. */
	public BlockGroup right() {
		return new BlockGroup(center.right(), Lists.transform(this.blocks, Block.right));
	}

	/** Create a new group, rotated clockwise. */
	public BlockGroup rotateClockwise() {
		return new BlockGroup(center, Lists.transform(this.blocks, new Function<Block, Block>() {
			@Override
			public Block apply(Block b) {
				int relative_x = b.x - center.x;
				int relative_y = b.y - center.y;
				int x = center.x - relative_y;
				int y = center.y + relative_x;
				return new Block(x, y, b.color);
			}
		}));
	}

	public static BlockGroup creationHelper(int x, int y, int[][] relativePoints, Block.Color color) {
		List<Block> blocks = new ArrayList<Block>();
		for (int i = 0; i < relativePoints.length; i++) {
			blocks.add(new Block(relativePoints[i][0] + x, relativePoints[i][1] + y, color));
		}
		return new BlockGroup(new Block(x, y, color), blocks);
	}

	public static BlockGroup newLine(int x, int y) {
		return creationHelper(x, y, new int[][] { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } },
				Block.Color.DARK_BLUE);
	}

	public static BlockGroup newSquare(int x, int y) {
		return creationHelper(x, y, new int[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } },
				Block.Color.RED);
	}

	public static BlockGroup newLShape1(int x, int y) {
		return creationHelper(x, y, new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
				Block.Color.ORANGE);
	}

	public static BlockGroup newLShape2(int x, int y) {
		return creationHelper(x, y, new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 1, -1 } },
				Block.Color.LIGHT_BLUE);
	}

	public static BlockGroup newNShape1(int x, int y) {
		return creationHelper(x, y, new int[][] { { -1, 0 }, { 0, 0 }, { 0, 1 }, { 1, 1 } },
				Block.Color.GREEN);
	}

	public static BlockGroup newNShape2(int x, int y) {
		return creationHelper(x, y, new int[][] { { -1, 0 }, { 0, 0 }, { 0, -1 }, { 1, -1 } },
				Block.Color.YELLOW);
	}

	public static BlockGroup newRandom(int x, int y) {
		int r = new Random().nextInt(6);
		if (r == 0) {
			return newLine(x, y);
		} else if (r == 1) {
			return newSquare(x, y);
		} else if (r == 2) {
			return newLShape1(x, y);
		} else if (r == 3) {
			return newLShape2(x, y);
		} else if (r == 4) {
			return newNShape1(x, y);
		} else {
			return newNShape2(x, y);
		}
	}

}
