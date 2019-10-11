package org.jakrabbit.tetris

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel

/** A simple container for x,y,color. */
class Block(val x: Int, val y: Int, val color: Color) {

    /** Create a new Block by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Block(x + dx, y + dy, color)

    /** Rotate this block clockwise around a point. */
    fun rotate(pX: Int, pY: Int) = Block(pX + (pY - y), pY + (x - pX), color)
}

/** A Piece is a list of blocks. */
class Piece(val blocks: List<Block>) {
    val center = blocks[1]

    /** Rotate this piece clockwise around its center. */
    fun rotate() = Piece(blocks.map { it.rotate(center.x, center.y) })

    /** Create a new Piece by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Piece(blocks.map { it.move(dx, dy) })

    companion object {
        private fun make(coords: List<Point>, color: Color) = Piece(coords.map { Block(it.x, it.y, color) })
        val lineShape = make(listOf(Point(0, -1), Point(0, 0), Point(0, 1), Point(0, 2)), Color.CYAN)
        val n1Shape = make(listOf(Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 1)), Color(0x3333FF))
        val n2Shape = make(listOf(Point(-1, 0), Point(0, 0), Point(0, -1), Point(1, -1)), Color.MAGENTA)
        val l1Shape = make(listOf(Point(-1, 0), Point(0, 0), Point(1, 0), Point(1, 1)), Color(0x2dd400))
        val l2Shape = make(listOf(Point(-1, 0), Point(0, 0), Point(1, 0), Point(1, -1)), Color.ORANGE)
        val t1Shape = make(listOf(Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 0)), Color.BLUE)
        val squareShape = make(listOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(0, 1)), Color(0xBB0000))
        val allShapes = listOf(lineShape, n1Shape, n2Shape, l1Shape, l2Shape, t1Shape, squareShape)
    }
}

/** The Tetris board */
data class Board(val blocks: List<Block>, val width: Int, val height: Int, val score: Int = 0,
                 private val piece: Piece? = null) {

    val currentPiece = piece ?: randomPiece()

    init {
        require(canFit(currentPiece)) { "current piece does not fit on this board" }
    }

    /** A mapping of completed rows to the number of points you get. */
    val scorePoints = mapOf(Pair(0, 0), Pair(1, 10), Pair(2, 25), Pair(3, 40), Pair(4, 55))

    /** Get the block at the given coordinates if there is one or null otherwise. */
    fun blockAt(x: Int, y: Int) = blocks.firstOrNull { it.x == x && it.y == y }

    /** Is the given row number complete? (i.e. are there blocks in every space?) .*/
    fun isRowComplete(y: Int) = (0 until width).all { blockAt(it, y) != null }

    /** Get the list of completed rows; the indexes of each row */
    fun findCompletedRows(): List<Int> = (0 until height).toList().filter { isRowComplete(it) }

    /** Is the given block within bounds and not overlapping any blocks? */
    fun canFit(b: Block) = b.x in (0 until width) && b.y >= 0 && b.y < height && blockAt(b.x, b.y) == null

    /** Is the given piece within bounds and not overlapping any blocks? */
    fun canFit(piece: Piece) = piece.blocks.all { canFit(it) }

    /** Remove the blocks from the given row and move all blocks above, down one. */
    private fun removeRow(y: Int) = with(blocks.filterNot { it.y == y }.map { if (it.y < y) it.move(0, 1) else it })

    private fun with(blocks: List<Block>) = Board(blocks, width, height, score, currentPiece)

    /** Create a new random piece at the top of the board. */
    fun randomPiece(): Piece = (Piece.allShapes).random().move((width / 2) - 1, 1)

    /** Is the given piece on the bottom? */
    fun isPieceOnBottom() = canFit(currentPiece) && !canFit(currentPiece.move(0, 1))

    fun isDone() = !canFit(randomPiece())

    /**
     * Pushes the current piece down potentially ending the game.
     */
    fun pushCurrentPieceDown(): Board {
        require(!isDone()) { "Game is over" }
        val completeRows = findCompletedRows()
        completeRows.forEach { removeRow(it) }
        val newBlocks = if (isPieceOnBottom()) blocks + currentPiece.blocks else blocks
        val piece = if (isPieceOnBottom()) randomPiece() else currentPiece.move(0, 1)
        val newScore = score + (scorePoints[completeRows.size] ?: 0)
        return Board(newBlocks, width, height, newScore, piece)
    }
}


/** Basic UI */
class Tetris(blockWidth: Int, blockHeight: Int) : JPanel() {

    private val BLOCK_SIZE = 30

    init {
        preferredSize = Dimension(blockWidth * BLOCK_SIZE + 1, blockHeight * BLOCK_SIZE + 1)
    }
    var board = Board(ArrayList(), blockWidth, blockHeight)
    val myKeyListener = object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.keyCode) {
                KeyEvent.VK_DOWN -> board = board.copy(piece = board.currentPiece.move(0, 1))
                KeyEvent.VK_UP -> board = board.copy(piece = board.currentPiece.rotate())
                KeyEvent.VK_LEFT -> board = board.copy(piece = board.currentPiece.move(-1, 0))
                KeyEvent.VK_RIGHT -> board = board.copy(piece = board.currentPiece.move(1, 0))
                KeyEvent.VK_SPACE ->
                    while (!board.isPieceOnBottom()) {
                        board = board.pushCurrentPieceDown()
                    }
            }
            repaint()
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = Color.WHITE
        g.fillRect(0, 0, this.width, this.height)
        paintBlocks(board.currentPiece.blocks, g)
        paintBlocks(board.blocks, g)
        g.color = Color.BLACK
        g.font = Font.decode("Arial-BOLD-18")
        g.drawString("Score: " + board.score, (this.width / 2) - 40, 20)
    }

    private fun paintBlocks(blocks: List<Block>, g: Graphics) {
        blocks.forEach {
            g.color = it.color
            g.fillRect(it.x * BLOCK_SIZE, it.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
            g.color = Color.BLACK
            g.drawRect(it.x * BLOCK_SIZE, it.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
        }
    }
}

fun main() {
    val main = JFrame("Tetris")
    main.isResizable = false
    main.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    main.setLocationRelativeTo(null)
    val canvas = Tetris(10, 20)
    main.addKeyListener(canvas.myKeyListener)
    main.add(canvas)
    main.pack()
    main.isVisible = true
    val pushDown = object : Thread() {
        override fun run() {
            while (!canvas.board.isDone()) {
                canvas.board = canvas.board.pushCurrentPieceDown()
                canvas.repaint()
                val sleepTime = (1000 - (canvas.board.score * 8)).coerceAtLeast(200)
                sleep(sleepTime.toLong())
            }
            print("Game is done!")
        }
    }
    pushDown.start()
}