package org.jakrabbit.tetris

import java.util.ArrayList
import java.util.Random
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Font
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame
import javax.swing.JPanel
import java.awt.event.KeyAdapter
import java.awt.Point


/** Pick a random element from a list. */
fun pickRandom<A>(list: List<A>) = list.get(rand.nextInt(list.size()))
private val rand = Random()


/**
 * A simple container for x,y,color.
 */
class Block(val x: Int, val y: Int, val color: Color) {

    /** Create a new Block by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Block(x + dx, y + dy, color)

    /** Rotate this block clockwise around a point. */
    fun rotate(pX: Int, pY: Int) = Block(pX + (pY - y), pY + (x - pX), color)
}


/**
 * A Piece is a list of blocks.
 */
class Piece(val blocks: List<Block>) {
    val center = blocks.get(1)

    /** Rotate this piece clockwise around its center. */
    fun rotate() = Piece(blocks.map { it.rotate(center.x, center.y) })

    /** Create a new Piece by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Piece(blocks.map { it.move(dx, dy) })

    class object {
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


/**
 * The Tetris board
 */
class Board(var blocks: List<Block>, val width: Int, val height: Int, var score: Int = 0) {

    var currentPiece: Piece = randomPiece()

    /** A mapping of completed rows to the number of points you get. */
    val scorePoints: Map<Int, Int> = mapOf(Pair(0, 0), Pair(1, 10), Pair(2, 25), Pair(3, 40), Pair(4, 55))

    /** Get the block at the given coordinates if there is one or null otherwise. */
    fun blockAt(x: Int, y: Int) = blocks.find { it.x == x && it.y == y }

    /** Is the given row number complete? (i.e. are there blocks in every space?) .*/
    fun isRowComplete(y: Int) = (0..width - 1).all { blockAt(it, y) != null }

    /** Get the list of completed rows; the indexes of each row */
    fun findCompletedRows() = (0..height - 1).toList().filter { isRowComplete(it) }

    /** Is the given block within bounds and not overlapping any blocks? */
    fun canFit(b: Block) = b.x >= 0 && b.x < width && b.y >= 0 && b.y < height && blockAt(b.x, b.y) == null

    /** Is the given piece within bounds and not overlapping any blocks? */
    fun canFit(piece: Piece) = piece.blocks.all { canFit(it) }

    /** Remove the blocks from the given row. */
    fun removeRow(y: Int): Unit {
        blocks = blocks.filterNot { it.y == y }.map { if (it.y < y) it.move(0, 1) else it }
    }

    /** Create a new random piece at the top of the board. */
    fun randomPiece(): Piece = pickRandom(Piece.allShapes).move((width / 2) - 1, 1)

    /** Is the given piece on the bottom? */
    fun isPieceOnBottom(piece: Piece) = canFit(piece) && !canFit(piece.move(0, 1))

    /**
     * Set the currentPiece if the given piece can fit.
     *
     * @return whether the piece fits.
     */
    fun setCurrentPiece(piece: Piece): Boolean {
        val result = canFit(piece)
        if (result) {
            currentPiece = piece
        }
        return result
    }

    /**
     * Pushes the current piece down potentially ending the game.
     *
     * @return whether the the game is done
     */
    fun pushCurrentPieceDown(): Boolean {
        val piece: Piece
        if (isPieceOnBottom(currentPiece)) {
            blocks = blocks.plus(currentPiece.blocks)
            val completeRows = findCompletedRows()
            completeRows.forEach { removeRow(it) }
            score += scorePoints.get(completeRows.size) ?: 0
            piece = randomPiece()
        } else {
            piece = currentPiece.move(0, 1)
        }
        return !setCurrentPiece(piece)
    }
}


/** Basic UI */
class Tetris(val blockWidth: Int, val blockHeight: Int) : JPanel() {
    val BLOCK_SIZE = 30
    val board = Board(ArrayList<Block>(), blockWidth, blockHeight)
    val myKeyListener = object:KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            when (e.getKeyCode()) {
                KeyEvent.VK_DOWN -> board.setCurrentPiece(board.currentPiece.move(0, 1))
                KeyEvent.VK_UP -> board.setCurrentPiece(board.currentPiece.rotate())
                KeyEvent.VK_LEFT -> board.setCurrentPiece(board.currentPiece.move(-1, 0))
                KeyEvent.VK_RIGHT -> board.setCurrentPiece(board.currentPiece.move(1, 0))
                KeyEvent.VK_SPACE ->
                    while (!board.isPieceOnBottom(board.currentPiece)) {
                        board.pushCurrentPieceDown()
                    }
                else -> Unit
            }
            repaint()
        }
    }

    constructor {
        setPreferredSize(Dimension(BLOCK_SIZE * blockWidth, BLOCK_SIZE * blockHeight))
        addKeyListener(myKeyListener)
    }

    override fun paintComponent(g: Graphics) {
        super<JPanel>.paintComponents(g)
        g.setColor(java.awt.Color.WHITE)
        g.fillRect(0, 0, this.getWidth(), this.getHeight())
        paintBlocks(board.currentPiece.blocks, g)
        paintBlocks(board.blocks, g)
        g.setColor(java.awt.Color.BLACK)
        g.setFont(Font.decode("Arial-BOLD-18"))
        g.drawString("Score: " + board.score, (this.getWidth() / 2) - 40, 20)
    }

    private fun paintBlocks(blocks: List<Block>, g: Graphics) {
        blocks.forEach {
            g.setColor(it.color)
            g.fillRect(it.x * BLOCK_SIZE, it.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
            g.setColor(java.awt.Color.BLACK)
            g.drawRect(it.x * BLOCK_SIZE, it.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
        }
    }
}


fun main(args: Array<String>) {
    val main = JFrame("Tetris")
    main.setResizable(false)
    main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    main.setLocationRelativeTo(null)
    val canvas: Tetris = Tetris(10, 20)
    main.addKeyListener(canvas.myKeyListener)
    main.add(canvas)
    main.pack()
    main.setVisible(true)
    val pushDown = object: Thread() {
        override fun run() {
            while (!canvas.board.pushCurrentPieceDown()) {
                canvas.repaint()
                val sleepTime = Math.max(1000 - (canvas.board.score * 8), 200)
                Thread.sleep(sleepTime.toLong())
            }
            print("Game is done!")
        }
    }
    pushDown.start()
}