import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

class BlockTest extends AssertionsForJUnit {
    @Test def testBlock = {
        var b = new Block(1, 1, "red")
        b.down
        assertEquals(2, b.y)
        var b2 = new Block(2, 3, "red")
        var b3 = b.add(b2);
        assertEquals(3, b3.x)
        assertEquals(5, b3.y)
    }

    @Test def testPiece = {
        val p = Piece.new_line(3, 4)
        val y = p.center.y
        p.down
        assertEquals(y + 1, p.center.y)
        p.rotate_clockwise
        assertEquals(p.blocks.map { b => (b.x, b.y) }, List((1, 0), (0, 0), (-1, 0), (-2, 0)))
        assertEquals(p.real_blocks.map { b => (b.x, b.y) }, List((4, 5), (3, 5), (2, 5), (1, 5)))
    }

    @Test def testPiece = {
        val rp = Piece.new_random_piece(0, 0)
        assertNotNull(rp)
    }

    @Test def testPieceCopy = {
        val p = Piece.new_line(3, 4)
        val pCopy = p.copy
        p.down
        p.left
        assertEquals(pCopy.center.x, p.center.x + 1)
        assertEquals(pCopy.center.y, p.center.y - 1)
    }

    @Test def testBoard = {
        val b = new Board(10, 20)
        
    }
}