Array.prototype.map = function(transformer) {
	var result = []
	for (var i=0; i<this.length; i++) {
		result.push(transformer(this[i]));
	}
	return result;
}

Array.prototype.filter = function(predicate) {
	var result = []
	for (var i=0; i<this.length; i++) {
		if (predicate(this[i])) {
			result.push(this[i]);
		}
	}
	return result;
}

Array.prototype.all = function(predicate) {
	for (var i=0; i<this.length; i++) {
		if (!predicate(this[i])) {
			return false;
		}
	}
	return true;
}

/**  Block object */
function Block(x, y, color) {
	this.x = x;
	this.y = y;
	this.color = color;
	this.down = function(){ return new Block(this.x, y + 1, this.color); }
	this.left = function(){ return new Block(this.x - 1, this.y, this.color); }
	this.right = function(){ return new Block(this.x + 1, this.y, this.color); }
}

/** Piece object */
function Piece(center, blocks) {
	this.center = center;
	this.blocks = blocks;
	this.down = function(){ 
		return new Piece(this.center.down(), this.blocks.map(function(b){ return b.down(); }));
	},
	this.left = function(){ 
		return new Piece(this.center.left(), this.blocks.map(function(b){ return b.left(); }));
	},
	this.right = function(){ 
		return new Piece(this.center.right(), this.blocks.map(function(b){ return b.right(); }));
	},
	this.rotate_clockwise = function() {
		var center = this.center;
		return new Piece(this.center, this.blocks.map(function(b){ 
            var relative_x = b.x - center.x;
			var relative_y = b.y - center.y;
            var x = center.x - relative_y
            var y = center.y + relative_x
            return new Block(x, y, b.color)
		}));
	}
}

function piece_helper(center, relative_coords){
	var blocks = [];
	for (var i=0; i<relative_coords.length; i++) {
		var coords = relative_coords[i];
		blocks.push(new Block(coords[0] + center.x, coords[1] + center.y, center.color));
	}
	return new Piece(center, blocks);
}

/** Create a new line piece. */
function line(x, y) {
	return piece_helper(new Block(x, y, "blue"), [[0,-1], [0,0], [0,1], [0,2]]);
}

/** Create a new square piece. */
function square(x, y) {
	return piece_helper(new Block(x, y, "#BB0000"), [[0, 0], [1,0], [1,1], [0,1]]);
}

/** Create a new square piece. */
function l_shape1(x, y) {
	return piece_helper(new Block(x, y, "#2dd400"), [[-1,0], [0,0], [1,0], [1,1]]);
}

/** Create a new square piece. */
function l_shape2(x, y) {
	return piece_helper(new Block(x, y, "#ff950c"), [[-1,0], [0,0], [1,0], [1,-1]]);
}

/** Create a new square piece. */
function n_shape1(x, y) {
	return piece_helper(new Block(x, y, "#2ea4ff"), [[-1,0], [0,0], [0,1], [1,1]]);
}

/** Create a new square piece. */
function n_shape2(x, y) {
	return piece_helper(new Block(x, y, "#4b0063"), [[-1,0], [0,0], [0,-1], [1,-1]]);
}

/** Pick a random element from an array. */
function pick_random(arr) {
	return arr[Math.floor(Math.random() * arr.length)];
}

function random_piece(x, y) {
	var constructors = [line, square, l_shape1, l_shape2, n_shape1, n_shape2];
	return pick_random(constructors)(x, y);
}


function Board(width, height) {
	this.current_piece = random_piece((width / 2) - 1, 1);
	this.placed_blocks = []
	this.width = width;
	this.height = height;
	this.score = 0;
	this.is_game_done = false;

	this.get_block_at = function(x, y) {
		for (var i=0; i<this.placed_blocks.length; i++) {
			var b = this.placed_blocks[i];
			if (b.x == x && b.y == y) {
				return b;
			}
		}
		return null;
	};

	this.get_row = function(y){
		var result = []
		for (var x=0; x<this.width; x++) {
			result.push(this.get_block_at(x, y));
		}
		return result;
	};

	this.is_row_complete = function(y) {
		return this.get_row(y).all(function(b) { return b != null; });
	};

	this.find_completed_rows = function() {
		var result = [];
		for (var y=0; y<this.height; y++) {
			if (this.is_row_complete(y)) {
				result.push(y);
			}
		}
		return result;
	};

	this.is_block_valid = function(b) {
		return b.x >= 0 && b.x < this.width && 
			b.y >= 0 && b.y < this.height && 
			this.get_block_at(b.x, b.y) == null;
	};

	this.is_piece_valid = function(piece) {
		var board = this;
		return piece.blocks.all(function(b) { 
			return board.is_block_valid(b); 
		});
	};
	
	this.place_piece = function(piece) {
		if (!this.is_piece_valid(piece)) {
			throw "Cannot place an invalid piece";
		}
		for (var i=0; i<piece.blocks.length; i++) {
			this.placed_blocks.push(piece.blocks[i]);
		}
	};

    this.remove_row = function(y) {
        this.placed_blocks = this.placed_blocks
			.filter(function(b){ return b.y != y; })
			.map(function(b) {
				if (b.y < y) {
					return b.down();
				} else {
					return b;
				}
			});
    }

    this.is_piece_on_bottom = function(piece) { 
		return !this.is_piece_valid(piece.down());
	};

	this.set_current_piece = function(piece){
		var valid = this.is_piece_valid(piece);
		if (valid){
			this.current_piece = piece;
		}
		return valid;
	};

	this.push_current_piece_down = function() {
        var result = false;
        if (this.is_piece_on_bottom(this.current_piece)) {
            this.place_piece(this.current_piece);
            var complete_rows = this.find_completed_rows();
			for (var i=0; i<complete_rows.length; i++) {
				this.remove_row(complete_rows[i]);
			}
            var score_points = {0:0, 1:10, 2:25, 3:40, 4:55}
            this.score += score_points[complete_rows.length];
            result = true;
            this.current_piece = random_piece((this.width / 2) - 1, 1);
        } else {
            this.current_piece = this.current_piece.down();
        }
        if (!this.is_piece_valid(this.current_piece)){
            alert("Game is Finished");
            this.is_game_done = true;
        }
        return result;
	};	
}


