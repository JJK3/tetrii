require './blocks'
require "test/unit"
 
class BlockTest < Test::Unit::TestCase
 
  def test_block
    b = Block.new(0,0, "red").down
    assert_equal(1, b.y)
  end

  def test_piece
    b = Block.new(0,0, "red")
    p = Piece.new_line(0, 0)
    assert_equal(p.real_blocks, [[0,-1], [0,0], [0,1], [0,2]].map{|x,y| Block.new(x,y, "red")} )
    p = p.rotate_clockwise
    assert_equal(p.real_blocks, [[1,0], [0,0], [-1,0], [-2,0]].map{|x,y| Block.new(x,y, "red")} )
  end

  def test_complete_rows
    b = Board.new(10, 20)
    (0..9).each{|x| 
      b.blocks << Block.new(x, 8, "red")
      b.blocks << Block.new(x, 9, "red")
    }
    assert_equal([8,9], b.find_completed_rows)
  end

  def test_push_down
    b = Board.new(10, 20)
    b.current_piece = Piece.new_line(5, 1)
    b.current_piece = b.current_piece.rotate_clockwise
    (1..18).each{|y|
      b.push_current_piece_down!
    }
    assert(b.push_current_piece_down!)
  end
end
