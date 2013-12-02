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

/** Pick a random element from a list. */
fun pickRandom<A>(list: List<A>) = list.get(rand.nextInt(list.size()))
private val rand = Random()

/**
 * A simple container for x,y,color.
 */
class Block(val x: Int, val y: Int, val color: Color){

    /** Create a new Block by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Block(x + dx, y + dy, color)

    /** Rotate this block clockwise around a point. */
    fun rotate(pX: Int, pY: Int) = Block(pX + (pY - y), pY + (x - pX), color)
}

/**
 * A Piece is a list of blocks.
 */
class Piece(val  blocks: jet.List<Block>){
    val center = blocks.get(1);

    /** Rotate this piece clockwise around its center. */
    fun rotate() = Piece(blocks.map { it.rotate(center.x, center.y) })

    /** Create a new Piece by adding the given coordinates to it. */
    fun move(dx: Int, dy: Int) = Piece(blocks.map { it.move(dx, dy) })
}

private fun make(coords: List<Pair<Int, Int>>, color: Color) =
        Piece(coords.map { Block(it.first, it.second, color) })
val lineShape = make(listOf(Pair(0, -1), Pair(0, 0), Pair(0, 1), Pair(0, 2)), Color.CYAN)
val n1Shape = make(listOf(Pair(-1, 0), Pair(0, 0), Pair(0, 1), Pair(1, 1)), Color(0x3333FF))
val n2Shape = make(listOf(Pair(-1, 0), Pair(0, 0), Pair(0, -1), Pair(1, -1)), Color.MAGENTA)
val l1Shape = make(listOf(Pair(-1, 0), Pair(0, 0), Pair(1, 0), Pair(1, 1)), Color(0x2dd400))
val l2Shape = make(listOf(Pair(-1, 0), Pair(0, 0), Pair(1, 0), Pair(1, -1)), Color.ORANGE)
val t1Shape = make(listOf(Pair(-1, 0), Pair(0, 0), Pair(0, 1), Pair(1, 0)), Color.BLUE)
val squareShape = make(listOf(Pair(0, 0), Pair(1, 0), Pair(1, 1), Pair(0, 1)), Color(0xBB0000))
val allShapes = listOf(lineShape, n1Shape, n2Shape, l1Shape, l2Shape, t1Shape, squareShape)


class Board(var blocks: List<Block>, val width: Int, val height: Int) {
    /** Current score. */
    var score = 0
    var current_piece: Piece

    /** A mapping of completed rows to the number of points you get. */
    val score_points: Map<Int, Int> = mapOf(Pair(0, 0), Pair(1, 10), Pair(2, 25), Pair(3, 40), Pair(4, 55))

    constructor {
        current_piece = randomPiece()
    }

    /** Get the block at the given coordinates if there is one or null otherwise. */
    fun blockAt(x: Int, y: Int) = blocks.find { it.x == x && it.y == y }

    /** Is the given row number complete? (i.e. are there blocks in every space?) .*/
    fun isRowComplete(y: Int) = (0..(width - 1)).all { blockAt(it, y) != null }

    /** Get the list of completed rows; the indexes of each row */
    fun findCompletedRows() = (0..(height - 1)).toList().filter { isRowComplete(it) }

    /** Is the given block within bounds? */
    fun canFit(b: Block) =
            b.x >= 0 && b.x < width && b.y >= 0 && b.y < height && blockAt(b.x, b.y) == null

    /** Is the given piece within bounds? */
    fun isPieceValid(piece: Piece) = piece.blocks.all { canFit(it) }

    /** Remove the blocks from the given row. */
    fun removeRow(y: Int): Unit {
        blocks = blocks.filterNot { it.y == y }.map {
            if (it.y < y) it.move(0, 1) else it
        }
    }

    fun randomPiece(): Piece = pickRandom(allShapes).move((width / 2) - 1, 1)

    /** Is the given piece on the bottom? */
    fun isPieceOnBottom(piece: Piece) = isPieceValid(piece) && !isPieceValid(piece.move(0, 1))

    fun setCurrentPiece(piece: Piece): Boolean {
        val result = isPieceValid(piece);
        if (result) {
            current_piece = piece
        }
        return result
    }

    /** Returns whether the the game is done */
    fun pushCurrentPieceDown(): Boolean {
        val piece: Piece
        if (isPieceOnBottom(current_piece)) {
            blocks = blocks.plus(current_piece.blocks)
            val complete_rows = findCompletedRows()
            complete_rows.forEach { removeRow(it) }
            score += score_points.get(complete_rows.size) ?: 0
            piece = randomPiece()
        } else {
            piece = current_piece.move(0, 1)
        }
        return setCurrentPiece(piece)
    }
}

/** Basic UI */
class Tetris(val blockWidth: Int, val blockHeight: Int) : JPanel() {
    val BLOCK_SIZE = 30
    val board = Board(ArrayList<Block>(), blockWidth, blockHeight)

    constructor {
        setPreferredSize(Dimension(BLOCK_SIZE * blockWidth, BLOCK_SIZE * blockHeight))
        addKeyListener(object:KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    board.setCurrentPiece(board.current_piece.move(0, 1))
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    board.setCurrentPiece(board.current_piece.rotate())
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    board.setCurrentPiece(board.current_piece.move(-1, 0))
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    board.setCurrentPiece(board.current_piece.move(1, 0))
                } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    while (!board.isPieceOnBottom(board.current_piece)) {
                        board.pushCurrentPieceDown()
                    }
                }
                repaint();
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super<JPanel>.paintComponents(g)
        g.setColor(java.awt.Color.WHITE)
        g.fillRect(0, 0, this.getWidth(), this.getHeight())
        paintBlocks(board.current_piece.blocks, g)
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
    main.addKeyListener(canvas.keyListener)
    main.add(canvas)
    main.pack()
    main.setVisible(true)
    val pushDown = object: Thread() {
        override fun run() {
            while (!canvas.board.pushCurrentPieceDown()) {
                canvas.repaint()
                var sleepTime = 1000 - (canvas.board.score * 6)
                if (sleepTime < 100){
                    sleepTime = 100
                }
                Thread.sleep(sleepTime.toLong())
            }
            print("Game is done!")
        }
    };
    pushDown.start();
}