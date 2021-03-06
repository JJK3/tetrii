import scala.util.Random
import java.awt.Color

/** An individual block */
class Block(val x: Int, val y: Int, val color: Color) {
	def move(dx: Int, dy: Int) = new Block(x + dx, y + dy)
	val down = move(0, 1)
    def add(other: Block) = new Block(x + other.x, y + other.y, color)
}

object Piece {
    val random = new Random()

    def new_line(x: Int, y: Int) = creation_helper(x, y, List((0, -1), (0, 0), (0, 1), (0, 2)), Color.CYAN)
    def new_square(x: Int, y: Int) = creation_helper(x, y, List((0, 0), (1, 0), (1, 1), (0, 1)), new Color(0xBB0000))
    def new_l_shape1(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (1, 0), (1, 1)), new Color(0x2dd400))
    def new_l_shape2(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (1, 0), (1, -1)), Color.ORANGE)
    def new_n_shape1(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (0, 1), (1, 1)), new Color(0x3333FF))
    def new_n_shape2(x: Int, y: Int) = creation_helper(x, y, List((-1, 0), (0, 0), (0, -1), (1, -1)), Color.MAGENTA)

    def new_random_piece(x: Int, y: Int): Piece = {
        val methods = List(new_line _, new_square _, new_l_shape1 _, new_l_shape2 _, new_n_shape1 _, new_n_shape2 _)
        return methods(random.nextInt(methods.size)).apply(x, y);
    }

    private def creation_helper(x: Int, y: Int, block_coords: List[(Int, Int)], color: Color): Piece = {
        val blocks = block_coords.map { (xy) => new Block(xy._1, xy._2, color) }
        return new Piece(new Block(blocks, color)).move(x, y)
    }
}

/** A group of blocks */
class Piece(val blocks: List[Block], val color: Color) {
    val center = blocks[1]
	val down = move(0, 1)
	def move(dx: Int, dy: Int) = new Piece(blocks.map { _.move(dx, dy) }, color)
    def rotate_clockwise = new Piece(center,
        blocks.map { (b) =>
            val relative_xy = (b.x - center.x, b.y - center.y)
            val x = center.x - relative_xy._2
            val y = center.y + relative_xy._1
            new Block(x, y, color)
        }, color);
}

class Board(val width: Int, val height: Int) {
    var blocks: List[Block] = Nil
    var score: Int = 0
    private var _current_piece = Piece.new_random_piece((width / 2) - 1, 1)
    var is_game_done = false

    def block_at(x: Int, y: Int) = blocks.find { (b) => b.x == x && b.y == y }

    /** Get the list of blocks that correspond to the given row. */
    def row(y: Int) = (0 to (width - 1)).map { block_at(_, y) }

    def is_row_complete(y: Int) = row(y).forall { _.isDefined }

    /** Get the list of completed rows; the indexes of each row */
    def find_completed_rows = (0 to (height - 1)).filter { is_row_complete(_) }

    /** Is the given block within bounds? */
    def is_block_valid(b: Block) =
        b.x >= 0 && b.x < width && b.y >= 0 && b.y < height && block_at(b.x, b.y).isEmpty

    /** Is the given piece within bounds? */
    def is_piece_valid(piece: Piece) = piece.blocks.forall { is_block_valid(_) }

    def place_piece(piece: Piece) = {
        require(is_piece_valid(piece), "Cannot place a piece that is invalid. " + piece)
        piece.blocks.foreach { blocks ::= _ }
    }

    def remove_row(y: Int) = {
        blocks = blocks.remove { _.y == y }.map { b => if (b.y < y) b.down else b }
    }

    def is_piece_on_bottom(piece: Piece) = !is_piece_valid(piece.down)

    def current_piece = _current_piece
    def current_piece_=(piece: Piece) = {
        if (is_piece_valid(piece))
            _current_piece = piece
    }

    /** Returns whether the piece was on the bottom after the push */
    def push_current_piece_down(): Boolean = {
        var result = false
        if (is_piece_on_bottom(current_piece)) {
            place_piece(current_piece)
            val complete_rows = find_completed_rows
            complete_rows.foreach { remove_row(_) }
            val score_points = Map[Int, Int](0 -> 0, 1 -> 10, 2 -> 25, 3 -> 40, 4 -> 55)
            score += score_points(complete_rows.size)
            result = true
            _current_piece = Piece.new_random_piece((width / 2) - 1, 1)
        } else {
            _current_piece = current_piece.down
        }
        if (!is_piece_valid(current_piece)){
            println("Game is Finished");
            is_game_done = true
        }
        return result
    }
}

