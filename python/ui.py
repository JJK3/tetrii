#!/usr/bin/env python

import pygtk
pygtk.require('2.0')
import gtk
import pango
import threading
import time
import logging
import sys
from blocks import *

class Tetris(threading.Thread):
    
    def __init__(self, board):
        self.block_size = 30
        self.board = board
        self.window = gtk.Window(gtk.WINDOW_TOPLEVEL)
        self.window.connect("destroy", self.destroy)
        self.window.set_size_request(board._width * self.block_size, board._height * self.block_size)
        self.area = gtk.DrawingArea()
        self.area.can_focus = True
        self.window.add(self.area)
        self.area.connect("expose_event", self.expose_callback)
        self.window.connect("key_press_event", self.keypress_callback)
        self.window.show_all()
        self.window.show()
        self.running = True
        threading.Thread.__init__(self)

    def destroy(self, widget, data=None):
        gtk.main_quit()
        self.running = False

    def expose_callback(self, widget, event, callback_data=None):
        self.area.window.draw_rectangle(self.area.style.white_gc, True, 0, 0, * self.area.window.get_size())
        for b in self.board._blocks: self.draw_block(b)
        if not(self.board.is_game_done()):
            for b in self.board.current_piece().blocks: self.draw_block(b)
        layout = self.area.create_pango_layout("Score: " + str(self.board._score))
        layout.set_font_description(pango.FontDescription("helvetica bold 20px"))
        self.area.window.draw_layout(self.area.style.black_gc, 100, 5, layout);

    def draw_block(self, block):
        gc = gtk.gdk.GC(self.area.window)
        gc.set_rgb_fg_color(gtk.gdk.Color(block.color))
        bs = self.block_size
        self.area.window.draw_rectangle(gc, True, block.x * bs, block.y * bs, bs, bs)  
        self.area.window.draw_rectangle(self.area.style.black_gc, False, block.x * bs, block.y * bs, bs, bs)

    def keypress_callback(self, widget, event, callback_data=None):
        cp = self.board.current_piece()
        if not(self.board.is_game_done()):
            if event.keyval == gtk.keysyms.Left: self.board.set_current_piece(cp.left())
            elif event.keyval == gtk.keysyms.Right: self.board.set_current_piece(cp.right())
            elif event.keyval == gtk.keysyms.Down: self.board.set_current_piece(cp.down())
            elif event.keyval == gtk.keysyms.Up: self.board.set_current_piece(cp.rotate_clockwise())
            elif event.keyval == gtk.keysyms.space: 
                while not(self.board.push_current_piece_down()): pass
        self.expose_callback(event, None)

    def run(self):
        time.sleep(1)
        while self.running and not(self.board.is_game_done()):
            time.sleep(0.7)
            if not(self.board.is_game_done()): 
                self.board.push_current_piece_down()  
                self.area.queue_draw()

if __name__ == "__main__":
    t = Tetris(Board(10, 20))
    t.start()
    gtk.gdk.threads_init()
    gtk.main()
    t.join()
