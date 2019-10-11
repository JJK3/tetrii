/** Create a range of integers. */
function range(start, end) {
	var result = [];
	for ( var i = start; i < end; i++) {
		result.push(i);
	}
	return result;
}

/** Pick a random element from an array. */
function pickRandom(arr) {
	return arr[Math.floor(Math.random() * arr.length)];
}

/**  Block object */
function Block(x, y, color) {
	this.x = x;
	this.y = y;
	this.color = color;

	/** Create a new Block moved from this block. */
	this.move = function(dx, dy) {
		return new Block(x + dx, y + dy, this.color);
	}

	/** Rotate this block clockwise around a point. */
	this.rotateClockwise = function(pX, pY) {
		return new Block(pX + (pY - y), pY + (x - pX), this.color);
	}
}

/** Piece object */
function Piece(blocks) {
	this.blocks = blocks;

	/** Create a new, moved Piece. */
	this.move = function(x, y) {
		return new Piece(this.blocks.map(function(b) {
			return b.move(x, y);
		}));
	},

	/** Rotate this block clockwise around a point. */
	this.rotateClockwise = function() {
		var center = this.blocks[1];
		return new Piece(this.blocks.map(function(b) {
			return b.rotateClockwise(center.x, center.y);
		}));
	}
}

function makePiece(color, coords) {
	return new Piece(coords.map(function(c) {
		return new Block(c[0], c[1], color);
	}));
}

var line = makePiece("#00eaff", [[0, -1], [0, 0], [0, 1], [0, 2]]);
var square = makePiece("#BB0000", [[0, 0], [1, 0], [1, 1], [0, 1]]);
var l_shape1 = makePiece("#2dd400", [[-1, 0], [0, 0], [1, 0], [1, 1]]);
var l_shape2 = makePiece("#ff950c", [[-1, 0], [0, 0], [1, 0], [1, -1]]);
var n_shape1 = makePiece("#2ea4ff", [[-1, 0], [0, 0], [0, 1], [1, 1]]);
var n_shape2 = makePiece("#4b0063", [[-1, 0], [0, 0], [0, -1], [1, -1]]);
var t_shape = makePiece("#0000BB", [[-1, 0], [0, 0], [0, -1], [1, 0]]);

function randomPiece(x, y) {
	var shapes = [line, square, l_shape1, l_shape2, n_shape1, n_shape2, t_shape];
	return pickRandom(shapes).move(x, y);
}

function Board(width, height) {
	this.currentPiece = randomPiece((width / 2) - 1, 1);
	this.placedBlocks = []
	this.width = width;
	this.height = height;
	this.score = 0;

	/** Get the block at the given coordinates or null if it doesn't exist. */
	this.getBlockAt = function(x, y) {
		for ( var i = 0; i < this.placedBlocks.length; i++) {
			var b = this.placedBlocks[i];
			if (b.x == x && b.y == y) {
				return b;
			}
		}
		return null;
	};

	/** Is the given row complete? i.e. is it filled with blocks? */
	this.isRowComplete = function(y) {
		for ( var x = 0; x < this.width; x++) {
			if (this.getBlockAt(x, y) == null) {
				return false;
			}
		}
		return true;
	};

	/** Get an array of indices of completed rows. */
	this.find_completed_rows = function() {
		return range(0, this.height).filter(this.isRowComplete, this);
	};

	/**
	 * Does the given block fit on the board.
	 * i.e. is it within bounds and doesn't overlap other blocks?
	 */
	this.doesBlockFit = function(b) {
		return b.x >= 0 && b.x < this.width && b.y >= 0 && b.y < this.height
				&& this.getBlockAt(b.x, b.y) == null;
	};

	/** Does the the given piece fit on the board? */
	this.doesPieceFit = function(piece) {
		return piece.blocks.every(this.doesBlockFit, this);
	};

	/** Add the blocks of the given piece to the board */
	this.placePiece = function(piece) {
		if (!this.doesPieceFit(piece)) {
			throw "Cannot place an invalid piece";
		}
		for ( var i = 0; i < piece.blocks.length; i++) {
			this.placedBlocks.push(piece.blocks[i]);
		}
	};

	/** Remove all the blocks on the given row and shift higher blocks down. */
	this.removeRow = function(y) {
		this.placedBlocks = this.placedBlocks
			.filter(function(b) { return b.y != y;})
			.map(function(b) {
				return (b.y < y) ? b.move(0, 1) : b;
			}
		);
	}

	/** Is the given piece touching any blocks beneath it? */
	this.isPieceOnBottom = function(piece) {
		return this.doesPieceFit(piece) && !this.doesPieceFit(piece.move(0, 1));
	};

	/** Set the current piece if it fits.  Returns whether the given piece fits or not. */
	this.setCurrentPiece = function(piece) {
		var valid = this.doesPieceFit(piece);
		if (valid) {
			this.currentPiece = piece;
		}
		return valid;
	};

	/**
	 * Pushes the current piece down and removes/scores any complete rows.
	 * Returns whether the game is done or not.
	 */
	this.push_currentPiece_down = function() {
		var result = false;
		if (this.isPieceOnBottom(this.currentPiece)) {
			this.placePiece(this.currentPiece);
			var completeRows = this.find_completed_rows();
			completeRows.forEach(this.removeRow, this);
			var score_points = {
				0 : 0,
				1 : 10,
				2 : 25,
				3 : 40,
				4 : 55
			}
			this.score += score_points[completeRows.length];
			result = true;
			this.currentPiece = randomPiece((this.width / 2) - 1, 1);
		} else {
			this.currentPiece = this.currentPiece.move(0, 1);
		}
		return !this.doesPieceFit(this.currentPiece);
	};
}
