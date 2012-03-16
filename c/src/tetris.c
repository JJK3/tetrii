#include <gtk/gtk.h>
#include <gdk/gdkdrawable.h>
#include <gdk/gdkkeysyms.h>
#include <stdlib.h>
#include <pthread.h>
#include "pieces.h"
#include <stdio.h>

#define BLOCK_SIZE 30

typedef struct _components {
	Board *board;
    GtkWidget *window;
    GtkWidget *mainPanel;
    GtkWidget *drawingArea;
    GdkPixmap *pixMap;
} components;

static components this;

static void createWindow() {
	this.board = board_create();
    this.window = gtk_window_new (GTK_WINDOW_TOPLEVEL);
	gtk_window_set_default_size(GTK_WINDOW(this.window), BLOCK_SIZE * WIDTH, BLOCK_SIZE * HEIGHT);
	gtk_window_set_position(GTK_WINDOW(this.window), GTK_WIN_POS_CENTER);
    gtk_window_set_title (GTK_WINDOW (this.window), "Tetris");
	gtk_signal_connect(GTK_OBJECT(this.window), "destroy", G_CALLBACK(gtk_main_quit), NULL);
}

static void layoutWidgets() {
    this.mainPanel = gtk_vbox_new(FALSE, 0);
    gtk_container_add  (GTK_CONTAINER (this.window), this.mainPanel);

    /* Add the draw-able area to the main panel. */
    gtk_box_pack_start (GTK_BOX(this.mainPanel), this.drawingArea, TRUE, TRUE, 0);
}

static void show() {
    gtk_widget_show (this.drawingArea);
    gtk_widget_show (this.mainPanel);
    gtk_widget_show (this.window);
}

/* Draw a rectangle on the screen */
static void
draw_block (GtkWidget *widget, Point * p)
{
	GdkRectangle update_rect;
	update_rect.x = p->x*BLOCK_SIZE;
	update_rect.y = p->y*BLOCK_SIZE;
	update_rect.width = BLOCK_SIZE;
	update_rect.height = BLOCK_SIZE;

	//	GdkColormap * cmap = gdk_colormap_get_system();
	GdkColor * color = malloc(sizeof(GdkColor));
	gdk_color_parse (p->color, color);

	GdkColor * white = malloc(sizeof(GdkColor));
	gdk_color_parse ("#FFFFFF", white);


	GdkGC *gc = widget->style->white_gc;
	//	gdk_gc_set_background(gc, &color);
	//	gdk_gc_set_foreground(gc, color);

	gdk_gc_set_rgb_fg_color (gc, color);
	gdk_draw_rectangle (this.pixMap,
						gc,
						TRUE,
						update_rect.x, update_rect.y,
						update_rect.width, update_rect.height);

	gdk_gc_set_rgb_fg_color (gc, white);
	gdk_draw_rectangle (this.pixMap,
						widget->style->black_gc,	
						FALSE,
						update_rect.x, update_rect.y,
						update_rect.width, update_rect.height);
}

static void
draw_piece (GtkWidget *widget, Piece * p)
{
	for (int i=0; i<4; i++){
		Point * relative_point = p->blocks[i];
		Point * real_point = point_create(relative_point->x + p->center->x,
										  relative_point->y + p->center->y);
		real_point->color = relative_point->color;
		draw_block(widget, real_point);
		point_free(real_point);
	}
}


/* Redraw the screen from the backing pixmap */
static gboolean
expose_event( GtkWidget *widget, GdkEventExpose *event )
{
	gdk_draw_drawable(widget->window,
					  widget->style->fg_gc[GTK_WIDGET_STATE (widget)],
					  this.pixMap,
					  event->area.x, event->area.y,
					  event->area.x, event->area.y,
					  event->area.width, event->area.height);
	return FALSE;
}

static void
board_redraw(GtkWidget *widget, Board * b)
{
	gdk_draw_rectangle (this.pixMap,
						widget->style->white_gc,
						TRUE,
						0, 0,
						widget->allocation.width,
						widget->allocation.height);

	draw_piece(widget, b->current_piece);
	for (int x=0; x<b->width; x++){
		for (int y=0; y<b->height; y++){
			if (b->placed_blocks[x][y] != NULL){
				draw_block(widget, board_find_piece_at(this.board, x, y));
			}
		}
	}
	gtk_widget_queue_draw_area (widget,
								0, 0,
								widget->allocation.width, widget->allocation.height);
}

/* Create a new backing pixmap of the appropriate size */
static gboolean
configure_event( GtkWidget *widget, GdkEventConfigure *event )
{
	if (this.pixMap)
		g_object_unref(this.pixMap);

	this.pixMap = gdk_pixmap_new(widget->window,
								 widget->allocation.width,
								 widget->allocation.height,
								 -1);

	gdk_draw_rectangle (this.pixMap,
						widget->style->white_gc,
						TRUE,
						0, 0,
						widget->allocation.width,
						widget->allocation.height);
	return TRUE;
}

/** Mutate a piece, but only if the result is valid (in bounds and not overlapping) */
bool mutate_if_valid(Piece * piece, void (*mutator) (Piece *)){
	Piece * copy = piece_copy(this.board->current_piece);
	(*mutator)(copy); //mutate the copy
	bool result = false;
	if (board_check_valid_placement(this.board, copy)){	
		(*mutator)(piece);
		result = true;
	}
	piece_free(copy);
	return result;
}

static gboolean
key_press_event( GtkWidget *widget, GdkEventKey *event, gpointer func_data )
{
	gboolean handled = TRUE;
	if (event->keyval == GDK_Left) {
		mutate_if_valid(this.board->current_piece, &piece_left);
	} else if (event->keyval == GDK_Right) {
		mutate_if_valid(this.board->current_piece, &piece_right);
	} else if (event->keyval == GDK_Up) {
		mutate_if_valid(this.board->current_piece, &piece_rotate_clockwise);
	} else if (event->keyval == GDK_Down) {
		board_push_current_piece_down(this.board);
	} else if (event->keyval == GDK_space) {
		while(mutate_if_valid(this.board->current_piece, &piece_down)){};
		board_push_current_piece_down(this.board);
	} else {
		handled = FALSE;
	}
	if (handled){
		board_redraw(widget, this.board);
	}
	return handled;
}

static void createDrawingArea() {
    this.drawingArea = gtk_drawing_area_new();
	GTK_WIDGET_SET_FLAGS (this.drawingArea, GTK_CAN_FOCUS);
    gtk_signal_connect (GTK_OBJECT (this.drawingArea), "expose_event",
						(GtkSignalFunc) expose_event, NULL);
    gtk_signal_connect (GTK_OBJECT(this.drawingArea),"configure_event",
						(GtkSignalFunc) configure_event, NULL);
    gtk_signal_connect (GTK_OBJECT (this.drawingArea), "key_press_event",
						(GtkSignalFunc) key_press_event, NULL);
    gtk_widget_set_events (this.drawingArea, GDK_EXPOSURE_MASK
						   | GDK_LEAVE_NOTIFY_MASK
						   | GDK_KEY_PRESS_MASK);
}

void *push_piece_down_thread()
{
	while(!this.board->is_done){
		sleep(1);
		board_push_current_piece_down(this.board);
		board_redraw(this.window, this.board);
	}
}

int main( int argc, char *argv[] )
{
	srand(time(NULL));
    gtk_init (&argc, &argv);
    createWindow();
    createDrawingArea();
    layoutWidgets();
    show();
	board_redraw(this.drawingArea, this.board);
	
	pthread_t thread1;
	int return1 = pthread_create( &thread1, NULL, push_piece_down_thread, NULL);

    gtk_main ();
    return 0;
}
