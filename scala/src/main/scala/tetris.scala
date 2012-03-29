import javax.swing._
import java.awt._
import java.awt.event._

object Tetris {
    def main(args: Array[String]) = {
        val main = new JFrame("Tetris");
        main.setResizable(false);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setLocationRelativeTo(null);
        val canvas: Tetris = new Tetris(10, 20);
        main.addKeyListener(canvas);
        main.add(canvas);
        main.pack();
        main.setVisible(true);
        val pushDown = new Thread() {
            override def run() {
                var sleepTime = 1000
                while (!canvas.board.is_game_done) {
                    try {
                        Thread.sleep(sleepTime);
                        sleepTime -= 10
                        canvas.board.push_current_piece_down()
                        canvas.repaint();
                    } catch {
                        case e: InterruptedException => Unit
                    }
                }
            };
        };
        pushDown.start();
    }
}

class Tetris(val blockWidth: Int, val blockHeight: Int) extends JPanel with KeyListener {
    final val BLOCK_SIZE = 30
    val board = new Board(blockWidth, blockHeight)

    this.setPreferredSize(new Dimension(BLOCK_SIZE * blockWidth, BLOCK_SIZE * blockHeight));
    this.addKeyListener(this);

    def keyPressed(e: KeyEvent) = {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            board.current_piece = board.current_piece.down
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            board.current_piece = board.current_piece.rotate_clockwise
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            board.current_piece = board.current_piece.left
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            board.current_piece = board.current_piece.right
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            while (!board.push_current_piece_down()) {
            }
        }
        this.repaint();
    }

    def keyReleased(e: KeyEvent) = {}

    def keyTyped(e: KeyEvent) = {}

    override def paintComponent(g: Graphics) = {
        super.paintComponents(g)
        g.setColor(java.awt.Color.WHITE)
        g.fillRect(0, 0, this.getWidth(), this.getHeight())
        paintBlocks(board.current_piece.blocks, g)
        paintBlocks(board.blocks, g)
        g.setColor(java.awt.Color.BLACK)
        g.setFont(Font.decode("Arial-BOLD-18"))
        g.drawString("Score: " + board.score, (this.getWidth() / 2) - 40, 20)
    }

    private def paintBlocks(bs: scala.List[Block], g: Graphics) = bs.foreach { paintBlock(_, g) }

    private def paintBlock(b: Block, g: Graphics) {
        g.setColor(b.color)
        g.fillRect(b.x * BLOCK_SIZE, b.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
        g.setColor(java.awt.Color.BLACK)
        g.drawRect(b.x * BLOCK_SIZE, b.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)
    }
}