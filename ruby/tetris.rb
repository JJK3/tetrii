require './blocks'
require 'gtk2'

# Simple UI using GTK
class TetrisUI

  def initialize(w, h, block_size=30)
    @block_size = block_size
    @board = Board.new(w, h)
    @window = Gtk::Window.new
    @window.signal_connect("destroy") { Gtk.main_quit }
    @window.set_size_request(@board.width * @block_size, @board.height * @block_size)
    @window.window_position = Gtk::Window::POS_CENTER
    @area = Gtk::DrawingArea.new
    @area.can_focus = true
    @window.add(@area).show_all
    @area.signal_connect('key_press_event'){ |w, e| key_pressed(@area, e) }
    @area.signal_connect('expose_event'){ |w, e| expose_event(@area, e) }
  end

  def key_pressed(area, event)
    if not(@board.game_over)
      if event.keyval == Gdk::Keyval::GDK_Left
        @board.current_piece = @board.current_piece.left
      elsif event.keyval == Gdk::Keyval::GDK_Right
        @board.current_piece = @board.current_piece.right
      elsif event.keyval == Gdk::Keyval::GDK_Down
        @board.current_piece = @board.current_piece.down
      elsif event.keyval == Gdk::Keyval::GDK_Up
        @board.current_piece = @board.current_piece.rotate_clockwise
      elsif event.keyval == Gdk::Keyval::GDK_space
        until @board.push_current_piece_down! do
        end
      end
    end
    area.queue_draw
  end

  def draw_block(area, block)
    gc = Gdk::GC.new(area.window)
    gc.rgb_fg_color = Gdk::Color.parse(block.color)
    bs = @block_size
    @area.window.draw_rectangle(gc, true, block.x*bs, block.y*bs, bs, bs)  
    @area.window.draw_rectangle(area.style.black_gc, false, block.x*bs, block.y*bs, bs, bs)  
  end

  def expose_event(area, event)
    @area.window.draw_rectangle(area.style.white_gc, true, 0, 0, area.window.size[0], area.window.size[1])
    @board.blocks.each{|b| draw_block(area, b) }
    @board.current_piece.blocks.each{|b| draw_block(area, b)} unless @board.game_over
    layout = Pango::Layout.new(Gdk::Pango.context)
    layout.text = "Score: #{@board.score}"
    layout.font_description = Pango::FontDescription.new("helvetica bold #{@block_size}px")
    @area.window.draw_layout(area.style.black_gc, @window.size[0]/2-@block_size*2, 5, layout);
  end

  def start
    Thread.new do
      until(@board.game_over)
        sleep(0.7)
        @board.push_current_piece_down! unless @board.game_over
        @area.queue_draw
      end
    end.abort_on_exception = true
    Gtk.main
  end
end

if __FILE__ == $0
  TetrisUI.new(10, 20, 30).start
end
