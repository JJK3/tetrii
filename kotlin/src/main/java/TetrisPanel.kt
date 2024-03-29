package org.jakrabbit.tetris

import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.lang.Thread.sleep
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.concurrent.thread

/** A simple container for x,y,color. */
data class Block(val x: Int, val y: Int, val color: Color) {

    /** Create a new Block by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Block(x + dx, y + dy, color)

    /** Rotate this block clockwise around a point. */
    fun rotate(pX: Int, pY: Int) = Block(pX + (pY - y), pY + (x - pX), color)
}

/** A Piece is a list of blocks. */
data class Piece(val blocks: List<Block>) {
    private val center = blocks[1]

    /** Create a new Piece by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Piece(blocks.map { it.move(dx, dy) })

    /** Rotate this piece clockwise around its center. */
    fun rotate() = Piece(blocks.map { it.rotate(center.x, center.y) })

    companion object {
        private fun make(color: Color, vararg coords: Point) = Piece(coords.map { Block(it.x, it.y, color) })
        private val lineShape = make(Color.CYAN, Point(0, -1), Point(0, 0), Point(0, 1), Point(0, 2))
        private val n1Shape = make(Color(0x3333FF), Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 1))
        private val n2Shape = make(Color.RED, Point(-1, 0), Point(0, 0), Point(0, -1), Point(1, -1))
        private val l1Shape = make(Color(0x2dd400), Point(-1, 0), Point(0, 0), Point(1, 0), Point(1, 1))
        private val l2Shape = make(Color.ORANGE, Point(-1, 0), Point(0, 0), Point(1, 0), Point(1, -1))
        private val t1Shape = make(Color.BLUE, Point(-1, 0), Point(0, 0), Point(0, 1), Point(1, 0))
        private val squareShape = make(Color.GREEN, Point(0, 0), Point(1, 0), Point(1, 1), Point(0, 1))

        fun random() = listOf(lineShape, n1Shape, n2Shape, l1Shape, l2Shape, t1Shape, squareShape).random()
    }
}

/** The Tetris board */
data class Board(
    val blocks: List<Block>,
    val width: Int,
    val height: Int,
    val score: Int = 0,
    val currentPiece: Piece = newRandomPiece(width)
) {

    /** A map of completed rows to the number of points you get. */
    private val scorePoints = mapOf(
        1 to 10,
        2 to 25,
        3 to 40,
        4 to 55
    )

    fun safelyMovePiece(piece: Piece) = if (canFit(piece)) copy(currentPiece = piece) else this

    /** Is this space empty? */
    fun isEmpty(x: Int, y: Int) = blocks.none { it.x == x && it.y == y }

    fun isGameOver() = !canFit(currentPiece)

    /** Is the given row number complete? (i.e. are there blocks in every space?) .*/
    fun isRowComplete(y: Int) = (0 until width).none { isEmpty(it, y) }

    /** Get the list of completed rows; the indexes of each row */
    fun findCompletedRows(): List<Int> = (0 until height).toList().filter { isRowComplete(it) }

    /** Is the current piece on the bottom? */
    fun isPieceOnBottom() = canFit(currentPiece) && !canFit(currentPiece.move(0, 1))

    /** Is the given block within bounds and not overlapping any blocks? */
    fun canFit(b: Block) = b.x in (0 until width) && b.y in (0 until height) && isEmpty(b.x, b.y)

    /** Is the given piece within bounds and not overlapping any blocks? */
    fun canFit(piece: Piece) = piece.blocks.all { canFit(it) }

    /** Remove the blocks from the given row and move all blocks above, down one. */
    fun removeRow(y: Int) = copy(blocks = blocks.filterNot { it.y == y }.map { if (it.y < y) it.move(0, 1) else it })

    /** Create a new random piece at the top of the board. */
    fun withNewPiece() = copy(currentPiece = newRandomPiece(width))

    /** Adds the current piece to the list of blocks and replaces the current piece with a new one */
    private fun placePiece() = copy(blocks = blocks + currentPiece.blocks).withNewPiece()

    /**
     * Pushes the current piece down potentially ending the game.
     */
    fun pushCurrentPieceDown() =
        if (isPieceOnBottom()) {
            var board = placePiece()
            val completeRows = board.findCompletedRows()
            for (y in completeRows) {
                board = board.removeRow(y)
            }
            val newScore = score + (scorePoints[completeRows.size] ?: 0)
            board.copy(score = newScore)
        } else {
            copy(currentPiece = currentPiece.move(0, 1))
        }

    companion object {
        private fun newRandomPiece(width: Int) = Piece.random().move((width / 2) - 1, 1)
    }
}


/** Swing UI */
class Tetris(width: Int, height: Int, val blockSize: Int) : JFrame("Tetris") {

    private var board = Board(emptyList(), width, height)

    private val myKeyListener = object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (!board.isGameOver()) {
                when (e.keyCode) {
                    KeyEvent.VK_DOWN -> board = board.safelyMovePiece(board.currentPiece.move(0, 1))
                    KeyEvent.VK_UP -> board = board.safelyMovePiece(board.currentPiece.rotate())
                    KeyEvent.VK_LEFT -> board = board.safelyMovePiece(board.currentPiece.move(-1, 0))
                    KeyEvent.VK_RIGHT -> board = board.safelyMovePiece(board.currentPiece.move(1, 0))
                    KeyEvent.VK_ESCAPE -> board = board.withNewPiece() // Cheater Cheater!!
                    KeyEvent.VK_SPACE -> {
                        val p = board.blocks.size
                        while (board.blocks.size == p) {
                            board = board.pushCurrentPieceDown()
                        }
                    }
                }
                repaint()
            }
        }
    }

    val panel = object : JPanel() {
        init {
            preferredSize = Dimension(width * blockSize + 1, height * blockSize + 1)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            g.color = Color.WHITE
            g.fillRect(0, 0, this.width, this.height)
            paintBlocks(board.currentPiece.blocks, g)
            paintBlocks(board.blocks, g)
            g.color = Color.BLACK
            g.font = Font.decode("Arial-BOLD-18").deriveFont(blockSize.toFloat() / 1.5f)
            g.drawString("Score: " + board.score, (this.width / 2) - (g.font.size * 2), blockSize)
        }

        private fun paintBlocks(blocks: List<Block>, g: Graphics) {
            blocks.forEach {
                g.color = it.color
                g.fillRect(it.x * blockSize, it.y * blockSize, blockSize, blockSize)
                g.color = Color.BLACK
                g.drawRect(it.x * blockSize, it.y * blockSize, blockSize, blockSize)
            }
        }
    }

    fun play() {
        isResizable = false
        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        addKeyListener(myKeyListener)
        add(panel)
        pack()
        isVisible = true
        thread {
            while (!board.isGameOver()) {
                board = board.pushCurrentPieceDown()
                panel.repaint()
                val sleepTime = (1000 - (board.score * 8)).coerceAtLeast(200)
                sleep(sleepTime.toLong())
            }
            print("Game over! You scored ${board.score} points!")
        }
    }
}

fun main() {
    Tetris(10, 20, 30).play()
}