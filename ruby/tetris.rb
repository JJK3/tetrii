require './blocks'
require 'gtk2'

BLOCK_SIZE = 30
$board = Board.new(10, 20)

window = Gtk::Window.new
window.signal_connect("destroy") { Gtk.main_quit }
window.set_size_request($board.width * BLOCK_SIZE, $board.height * BLOCK_SIZE)
window.window_position = Gtk::Window::POS_CENTER
area = Gtk::DrawingArea.new
area.can_focus = true
area.add_events(Gdk::Event::KEY_PRESS_MASK)
window.add(area)
window.show_all

def key_pressed(area, event)
  if not($board.game_over)
    if event.keyval == Gdk::Keyval::GDK_Left
      $board.mutate_if_valid{|p| p.left!}
    elsif event.keyval == Gdk::Keyval::GDK_Right
      $board.mutate_if_valid{|p| p.right!}
    elsif event.keyval == Gdk::Keyval::GDK_Down
      $board.mutate_if_valid{|p| p.down!}
    elsif event.keyval == Gdk::Keyval::GDK_Up
      $board.mutate_if_valid{|p| p.rotate_clockwise!}
    elsif event.keyval == Gdk::Keyval::GDK_space
      until $board.push_current_piece_down! do
      end
    end
  end
  expose_event(area, event)
end

def draw_block(area, block)
  gc = Gdk::GC.new(area.window)
  gc.rgb_fg_color = Gdk::Color.parse(block.color)
  area.window.draw_rectangle(gc, true, block.x*BLOCK_SIZE, block.y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)  
  area.window.draw_rectangle(area.style.black_gc, false, block.x * BLOCK_SIZE, block.y * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE)  
end

def expose_event(area, event)
  area.window.draw_rectangle(area.style.white_gc, true, 0, 0, area.window.size[0], area.window.size[1])
  $board.blocks.each{|b| draw_block(area, b) }
  $board.current_piece.real_blocks.each{|b| draw_block(area, b) }
  layout = Pango::Layout.new(Gdk::Pango.context)
  layout.text = "Score: #{$board.score}"
  layout.font_description = Pango::FontDescription.new("helvetica bold 20px")
  area.window.draw_layout(area.style.black_gc, 100, 5, layout);

end

Thread.new do
  sleep(1)
  until($board.game_over)
    begin
      sleep(0.7)
      $board.push_current_piece_down! unless $board.game_over
      expose_event(area, nil)
    rescue
      puts $!
    end
  end
end

area.signal_connect('key_press_event')     { |w, e| key_pressed(   area, e) }
area.signal_connect('expose_event')        { |w, e| expose_event(  area, e) }
Gtk.main
