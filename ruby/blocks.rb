# An individual block
class Block
  attr_accessor :x, :y, :color
  
  def initialize(x, y, color)
    @x = x
    @y = y
    @color = color
  end

  # Create a new block, shifted down
  def down() Block.new(@x, @y+1, @color) end

  # Create a new block, shifted left
  def left() Block.new(@x-1, @y, @color) end

  # Create a new block, shifted right
  def right() Block.new(@x+1, @y, @color) end
  
  # Create a new block by rotating around another
  def rotate_clockwise(other)
    x = other.x - (@y - other.y)
    y = other.y + (@x - other.x)
    Block.new(x, y, @color)
  end

  def ==(other)
    other.is_a?(Block) and other.x == @x and other.y == @y
  end

  def to_s() "Block(#{@x},#{@y})" end
end

# A group of blocks
class Piece
  attr_reader :center, :blocks

  def initialize(center, blocks)
    @center = center
    @blocks = blocks
  end

  def down() move_help{|b| b.down} end
  def left() move_help{|b| b.left} end
  def right() move_help{|b| b.right} end
  def rotate_clockwise() move_help{|b| b.rotate_clockwise(@center) } end 
  def self.new_line(x, y)     creation_helper(x, y, [[0,-1], [0,0], [0,1], [0,2]], "blue") end
  def self.new_square(x, y)   creation_helper(x, y, [[0, 0], [1,0], [1,1], [0,1]], "#BB0000") end
  def self.new_l_shape1(x, y) creation_helper(x, y, [[-1,0], [0,0], [1,0], [1,1]], "#2dd400") end
  def self.new_l_shape2(x, y) creation_helper(x, y, [[-1,0], [0,0], [1,0], [1,-1]], "#ff950c") end
  def self.new_n_shape1(x, y) creation_helper(x, y, [[-1,0], [0,0], [0,1], [1,1]], "#2ea4ff") end
  def self.new_n_shape2(x, y) creation_helper(x, y, [[-1,0], [0,0], [0,-1], [1,-1]], "#4b0063") end
  def self.new_random_piece(x, y)
    methods = [:new_line, :new_square, :new_l_shape1, :new_l_shape2, :new_n_shape1, :new_n_shape2]
    Piece.send(methods[rand(methods.size)], x, y)
  end

  def ==(other)
    other.is_a?(Piece) and other.blocks.sort == self.blocks.sort
  end

  def to_s() 
    "Piece(center:#{@center}, blocks:#{@blocks.map{|b| b.to_s}.join(", ")})" 
  end
  
  private
  def move_help(&block) 
    Piece.new(yield(@center), @blocks.map{|b| yield(b)}) 
  end

  def self.creation_helper(x, y, block_coords, color)
    blocks = block_coords.map{|x1, y1| Block.new(x1+x, y1+y, color)}
    return Piece.new(Block.new(x, y, color), blocks)
  end
end


class Board
  attr_accessor :current_piece
  attr_reader :blocks, :width, :height, :score, :game_over
  
  def initialize(width, height)
    @width = width
    @height = height
    @blocks = []
    @score = 0
    @current_piece = Piece.new_random_piece((@width/2)-1, 1)
    @game_over = false
  end

  def block_at(x, y)
    @blocks.find(nil){|b| b.x == x and b.y == y}
  end

  def row(y) 
    (0..@width-1).map{|x| block_at(x,y)} 
  end

  def is_row_complete(y) 
    row(y).all?{|b| b != nil}
  end

  # Get the list of completed rows; the indexes of each row
  def find_completed_rows
    (0..@height-1).select{|y| is_row_complete(y)}
  end

  def check_valid_placement(piece)
    piece.blocks.all?{|b|
      b.x >= 0 and b.x < @width and b.y >= 0 and b.y < @height and block_at(b.x, b.y).nil?
    }
  end

  def place_piece!(piece)
    raise "Cannot place a piece that is invalid. #{piece}" unless check_valid_placement(piece)
    piece.blocks.each{|b| @blocks << b }
  end
  
  def remove_row!(y)
    @blocks.reject!{|b| b.y == y}.map!{|b| if b.y < y then b.down else b end }
  end

  def is_piece_on_bottom(piece)
    not(check_valid_placement(piece.down))
  end

  def current_piece=(piece)
    @current_piece = piece if check_valid_placement(piece)
  end

  # Returns whether the piece was on the bottom after the push
  def push_current_piece_down!
    result = false
    if is_piece_on_bottom(@current_piece)
      place_piece!(@current_piece) 
      complete_rows = find_completed_rows
      complete_rows.each{|row| remove_row!(row) }
      score_points = {0=>0, 1=>10, 2=>20, 3=>30, 4=>55}
      @score += score_points[complete_rows.size]
      result = true
      @current_piece = Piece.new_random_piece((@width/2)-1, 1)
      if not(check_valid_placement(@current_piece))
        @game_over = true
        puts "Game is over.  Final score:#{@score}"
      end
    end
    self.current_piece = self.current_piece.down
    return result
  end
end

