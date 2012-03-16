#include <stdbool.h>

#define WIDTH 10
#define HEIGHT 20

#ifndef PIECES_H
#define PIECES_H

typedef struct LinkedList {
  void * car;
  struct LinkedList * cdr;
} LinkedList ;

typedef struct {
  int x; 
  int y;
} Point ;

typedef struct {
  /* The center point that blocks will rotate around */
  Point * center; 
  Point ** blocks;
} Piece ;

/** A board where (0,0) is on the top-left of the board. */
typedef struct {
  int height;
  int width;
  int score;
  Piece * current_piece;
  Point * placed_blocks[WIDTH][HEIGHT];
} Board ;





/** Linked List functions */
LinkedList * linked_list_create();
void linked_list_free(LinkedList * pl);
LinkedList * linked_list_cons(LinkedList * list, void * piece);



/** Point functions */
Point * point_create(int x, int y);
void point_free(Point *p);
bool point_equals(Point *p1, Point *p2);
void point_print(Point *p);



/** Piece functions */
Piece * line(int x, int y);
Piece * square(int x, int y);
Piece * l_shape1(int x, int y);
Piece * l_shape2(int x, int y);
Piece * n_shape1(int x, int y);
Piece * n_shape2(int x, int y);
Piece * piece_create(int center_x, int center_y, int coords[4][2]);
void piece_down(Piece* p);
void piece_free(Piece* p);
void piece_left(Piece *p);
void piece_right(Piece *p, Board * b);
void piece_rotate_clockwise(Piece *p);
void piece_rotate_counter_clockwise(Piece *p);
bool piece_equals(Piece *p1, Piece *p2);
Piece * piece_create_random(int x, int y);
int piece_rightmost_block(Piece * p);
int piece_leftmost_block(Piece * p);


/** Board functions */
Board * board_create();
void board_free (Board * b);
bool board_is_row_complete(Board * b, int row);
bool * board_find_completed_rows(Board * b);
void board_place_piece(Board * b, Piece * p);
bool board_check_valid_placement(Board * b, Piece * p);
bool piece_contains_point(Piece * piece, Point * test_point);
bool board_push_current_piece_down(Board * b);
bool board_can_piece_move_down(Board * b);
Point * board_find_piece_at(Board * b, int x, int y);

#endif /* PIECES_H */

