import random

# A simple tuple of x,y,color
class Block:
    def __init__(self, x, y, color):
        self.x = x
        self.y = y
        self.color = color

    # Create a new Block, shifted down
    def down(self): return Block(self.x, self.y + 1, self.color)

    # Create a new Block, shifted left
    def left(self): return Block(self.x - 1, self.y, self.color)

    # Create a new Block, shifted right
    def right(self): return Block(self.x + 1, self.y, self.color)

    def __repr__(self): return "Block(%d, %d, %s)" % (self.x, self.y, self.color)


# Represents a logical grouping of Blocks.
# A Piece can be moved around and rotated as a group.
class Piece:
    def __init__(self, center, blocks):
        self.center = center
        self.blocks = blocks

    def _create_helper(self, func): return Piece(func(self.center), map(func, self.blocks))        

    def down(self): return self._create_helper(Block.down)

    def left(self): return self._create_helper(Block.left)

    def right(self): return self._create_helper(Block.right)

    def rotate_clockwise(self): 
        def rotator(b):
            x = self.center.x - (b.y - self.center.y)
            y = self.center.y + (b.x - self.center.x)
            return Block(x, y, self.center.color)
        return self._create_helper(rotator)

    @staticmethod
    def _create_helper2(x, y, color, coords): 
        return Piece(Block(x, y, color), [Block(a+x, b+y, color) for (a,b) in coords])

    @staticmethod
    def line(x, y): return Piece._create_helper2(x, y, "#0099FF", [[0, -1], [0, 0], [0, 1], [0, 2]])

    @staticmethod
    def square(x, y): return Piece._create_helper2(x, y, "red", [[0, 0], [1, 0], [1, 1], [0, 1]])

    @staticmethod
    def l_shape1(x, y): return Piece._create_helper2(x, y, "yellow", [[-1, 0], [0, 0], [1, 0], [1, 1]])

    @staticmethod
    def l_shape2(x, y): return Piece._create_helper2(x, y, "orange", [[-1, 0], [0, 0], [1, 0], [1, -1]])

    @staticmethod
    def n_shape1(x, y): return Piece._create_helper2(x, y, "green", [[-1, 0], [0, 0], [0, 1], [1, 1]])

    @staticmethod
    def n_shape2(x, y): return Piece._create_helper2(x, y, "brown", [[-1, 0], [0, 0], [0, -1], [1, -1]])

    # The list of all named constructors.
    @staticmethod
    def creators(): return [Piece.line, Piece.square, Piece.l_shape1, Piece.l_shape2, Piece.n_shape1, Piece.n_shape2]

    # Create a new random piece
    @staticmethod
    def random_piece(x, y): return random.choice(Piece.creators())(x, y)


class Board:

    def __init__(self, width, height):
        self._blocks = []
        self._score = 0
        self._current_piece = None
        self._is_game_done = False
        self._width = width
        self._height = height
        self._current_piece = Piece.random_piece((width / 2) - 1, 1)

    def block_at(self, x, y): return next((b for b in self._blocks if b.x == x and b.y == y), None)

    # Get the list of blocks that correspond to the given row.
    def row(self, y): return [self.block_at(x, y) for x in range(0, self._width)]

    def is_row_complete(self, y): return all([b != None for b in self.row(y)])

    # Get the list of completed rows; the indexes of each row 
    def find_complete_rows(self): return [y for y in range(0, self._height) if self.is_row_complete(y)]

    # Is the given block within bounds? 
    def is_block_valid(self, b):
        return 0 <= b.x < self._width and 0 <= b.y < self._height and self.block_at(b.x, b.y) == None

    # Is the given piece within bounds?
    def is_piece_valid(self, piece): return all([self.is_block_valid(b) for b in piece.blocks])

    def place_piece(self, piece):
        if not(self.is_piece_valid(piece)): raise ValueError("Cannot place a piece that is invalid. " + str(piece))
        for b in piece.blocks: self._blocks.append(b)
    
    def remove_row(self, y):
        for b in self.row(y): self._blocks.remove(b)
        #self.blocks = [b for b in self.blocks if b.y != y]
        self._blocks = [b.down() if b.y < y else b for b in self._blocks]

    def is_piece_on_bottom(self, piece): return not(self.is_piece_valid(piece.down()))

    def set_current_piece(self, piece):
        if (self.is_piece_valid(piece)):
            self._current_piece = piece
    def current_piece(self): return self._current_piece

    def is_game_done(self): return self._is_game_done;

    # Returns whether the piece was on the bottom after the push 
    def push_current_piece_down(self):
        result = False
        if (self.is_piece_on_bottom(self._current_piece)):
            self.place_piece(self._current_piece)
            complete_rows = self.find_complete_rows()
            for y in complete_rows: self.remove_row(y)
            score_points = {0:0, 1:10, 2:25, 3:40, 4:55}
            self._score += score_points[len(complete_rows)]
            result = True
            self._current_piece = Piece.random_piece((self._width / 2) - 1, 1)
        else:
            self._current_piece = self._current_piece.down()
        if not(self.is_piece_valid(self._current_piece)):
            print("Game is Finished");
            self._is_game_done = True
        return result
