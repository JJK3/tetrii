// An individual block
class Block(var x: Int, var y: Int, val color: String) {
    def down = { y += 1 }
    def left = { x -= 1 }
    def right = { x += 1 }
    override def toString = "Block(//{@x},//{@y})"
    def copy = { new Block(x, y, color) }
    def add(other: Block) = new Block(x + other.x, y + other.y, color)
    /*  def ==(other)
    other.is_a?(Block) and other.x == @x and other.y == @y
  }
  */
}

object Piece {
    def new_line(x: Int, y: Int) = creation_helper(x, y, List((0, -1), (0, 0), (0, 1), (0, 2)), "blue")
    def new_square(x: Int, y: Int) = creation_helper(x, y, List((0, 0), (1, 0), (1, 1), (0, 1)), "//BB0000")
    def new_l_shape1(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (1, 0), (1, 1)), "//2dd400")
    def new_l_shape2(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (1, 0), (1, -1)), "//ff950c")
    def new_n_shape1(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (0, 1), (1, 1)), "//2ea4ff")
    def new_n_shape2(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (0, -1), (1, -1)), "//4b0063")

    def new_random_piece(x: Int, y: Int): Piece = {
        /*methods = [:new_line, :new_square, :new_l_shape1, :new_l_shape2, :new_n_shape1, :new_n_shape2]
    Piece.send(methods[rand(methods.size)], x, y)*/
        return null;
    }

    private def creation_helper(x: Int, y: Int, block_coords: List[(Int, Int)], color: String): Piece = {
        val blocks = block_coords.map { (xy) => new Block(xy._1, xy._2, color) }
        return new Piece(new Block(x, y, color), blocks, color)
    }
}

// A group of blocks
class Piece(var center: Block, var blocks: List[Block], val color: String) {
    def down = center.down
    def left = center.left
    def right = center.right
    def rotate_clockwise = blocks.foreach { (b) =>
        var tmp_x = b.x
        b.x = -b.y
        b.y = tmp_x
    }
    override def toString = "Piece(center://{@center}, blocks://{@blocks.map{|b| b.to_s}.join(', ')})"

    /** since blocks are relative to the center, get blocks with the real coords */
    def real_blocks = blocks.map { _.add(center) }

    def copy = new Piece(center.copy, blocks.map { _.copy }, color)

    /*  def ==(other)
    other.is_a?(Piece) and other.real_blocks.sort == self.real_blocks.sort
  }
  */
}

class Board(val width: Int, val height: Int) {
    var blocks: List[Block] = Nil
    var score: Int = 0
    var current_piece = Piece.new_random_piece((width / 2) - 1, 1)

    def block_at(x: Int, y: Int) = blocks.find { (b) => b.x == x && b.y == y }.getOrElse(null)
    def row(y: Int) = (0.to(width - 1)).map { block_at(_, y) }

    def is_row_complete(y: Int) = {
        !row(y).contains(null)
    }

    override def toString = {
        var s = ""
        (0.to(height - 1)).foreach { y =>
            row(y).foreach { b => s += (if (b == null) " ." else " X") }
            s += "\n"
        }
        s
    }

    // Get the list of completed rows; the indexes of each row
    def find_completed_rows = {
        (0.to(height - 1)).filter { is_row_complete(_) }
    }

    def check_valid_placement(piece: Piece) = {
        piece.real_blocks.forall { b =>
            b.x >= 0 && b.x < width && b.y >= 0 && b.y < height && block_at(b.x, b.y) == null
        }
    }

    def place_piece(piece: Piece) = {
        if (!check_valid_placement(piece)) {
            throw new IllegalArgumentException("Cannot place a piece that is invalid. //{piece}")
        }
        piece.real_blocks.foreach { blocks ::= _ }

    }

    def remove_row(y: Int) = {
        System.out.println("removing row //{y}")
        blocks.remove { row(y).contains(_) }
        (0.to(y - 1)).foreach {
            row(_).foreach { b => if (b != null) { b.down } }
        }
    }

    // Test whether the given operation results in a valid piece placement.
    def is_operation_valid(piece: Piece, block: (Piece) => Unit): Boolean = {
        val test_piece = piece.copy
        block(test_piece)
        return check_valid_placement(test_piece)
    }

    // Mutate the current piece, but only if the operation is valid
    def mutate_if_valid(block: (Piece) => Unit) = {
        if (is_operation_valid(current_piece, (b: Piece) => block(b))) {
            block(current_piece)
        }
    }

    def is_piece_on_bottom(piece: Piece) = {
        !is_operation_valid(piece, (p: Piece) => p.down)
    }

    // Returns whether the piece was on the bottom after the push
    def push_current_piece_down: Boolean = {
        var result = false
        if (is_piece_on_bottom(current_piece)) {
            place_piece(current_piece)
            val complete_rows = find_completed_rows
            complete_rows.foreach { remove_row(_) }
            val score_points = Map[Int, Int](0 -> 0, 1 -> 10, 2 -> 20, 3 -> 30, 4 -> 55)
            score += score_points(complete_rows.size)
            result = true
            current_piece = Piece.new_random_piece((width / 2) - 1, 1)
        }
        mutate_if_valid((p: Piece) => p.down)
        return result
    }
}

