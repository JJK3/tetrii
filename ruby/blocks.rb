# An individual block
class Block
  attr_accessor :x, :y, :color
  
  def initialize(x, y, color)
    @x = x
    @y = y
    @color = color
  end

  def down!() @y += 1 end
  def left!() @x -= 1 end
  def right!() @x += 1 end
  def to_s() "Block(#{@x},#{@y})" end
  def copy() Block.new(@x, @y, @color) end 
  def add(other) Block.new(@x+other.x, @y+other.y, @color) end
  def ==(other)
    other.is_a?(Block) and other.x == @x and other.y == @y
  end
end

# A group of blocks
class Piece
  attr_reader :center

  def initialize(center, blocks, color)
    @center = center
    @blocks = blocks
    @color = color
    @blocks.each{|b| b.color = color}
  end

  def down!() @center.down! end
  def left!() @center.left! end
  def right!() @center.right! end
  def rotate_clockwise!() @blocks.each{|b| b.x, b.y = -b.y, b.x} end
  def to_s() "Piece(center:#{@center}, blocks:#{@blocks.map{|b| b.to_s}.join(", ")})" end

  #since blocks are relative to the center, get blocks with the real coords
  def real_blocks() @blocks.map{|b| b.add(@center)} end

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

  def copy
    Piece.new(@center.copy, @blocks.map{|b| b.copy}, @color)
  end

  def ==(other)
    other.is_a?(Piece) and other.real_blocks.sort == self.real_blocks.sort
  end
  
  private
  def self.creation_helper(x, y, block_coords, color)
    blocks = block_coords.map{|x1, y1| Block.new(x1, y1, color)}
    return Piece.new(Block.new(x, y, color), blocks, color)
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
    not(row(y).include?(nil))
  end

  def to_s
    s = ""
    for y in 0..@height-1
      row(y).each{|b| s+= b.nil? ? " ." : " X" }
      s += "\n"
    end
    s
  end

  # Get the list of completed rows; the indexes of each row
  def find_completed_rows
    (0..@height-1).select{|y| is_row_complete(y)}
  end

  def check_valid_placement(piece)
    piece.real_blocks.all?{|b|
      b.x >= 0 and b.x < @width and b.y >= 0 and b.y < @height and block_at(b.x, b.y).nil?
    }
  end

  def place_piece!(piece)
    raise "Cannot place a piece that is invalid. #{piece}" unless check_valid_placement(piece)
    piece.real_blocks.each{|b| @blocks << b }
  end
  
  def remove_row(y)
    puts "removing row #{y}"
    row(y).each{|b| @blocks.delete(b) }
    (0..y-1).each{|row_num|  
      row(row_num).compact.each{|b| b.down!}
    }
  end

  # Test whether the given operation results in a valid piece placement.
  def is_operation_valid(piece, &block)
    test_piece = piece.copy
    block.call(test_piece)
    return check_valid_placement(test_piece)
  end

  # Mutate the current piece, but only if the operation is valid
  def mutate_if_valid(&block)
    if (is_operation_valid(@current_piece){|p| block.call(p) })
      block.call(@current_piece)
    end
  end

  def is_piece_on_bottom(piece)
    not(is_operation_valid(piece){|p| p.down! })
  end

  # Returns whether the piece was on the bottom after the push
  def push_current_piece_down!
    result = false
    if is_piece_on_bottom(@current_piece)
      place_piece!(@current_piece) 
      complete_rows = find_completed_rows
      complete_rows.each{|row| remove_row(row) }
      score_points = {0=>0, 1=>10, 2=>20, 3=>30, 4=>55}
      @score += score_points[complete_rows.size]
      result = true
      @current_piece = Piece.new_random_piece((@width/2)-1, 1)
      if not(check_valid_placement(@current_piece))
        @game_over = true
        puts "Game is over.  Final score:#{@score}"
      end
    end
    mutate_if_valid{|p| p.down!}
    return result
  end
end

