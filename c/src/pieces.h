#include <stdbool.h>

#ifndef PIECES_H
#define PIECES_H

//typedef struct Point Point;
typedef struct {
  int x; 
  int y;
} Point ;

typedef struct {
  /* The center point that blocks will rotate around */
  Point * center; 
  Point ** blocks;
} Piece ;

typedef struct LinkedList {
  void * car;
  struct LinkedList * cdr;
} LinkedList ;

LinkedList * linked_list_create();
void linked_list_free(LinkedList * pl);
LinkedList * linked_list_cons(LinkedList * list, void * piece);


/** A board where (0,0) is on the top-left of the board. */
typedef struct {
  int height;
  int width;
  Piece * current_piece;
  LinkedList * placed_pieces;
} Board ;

/** Point functions */
Point * point_create(int x, int y);
bool point_equals(Point *p1, Point *p2);
// TODO: write a test for this...
bool piece_contains_point(Piece * piece, Point * test_point);
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
void piece_left(Piece *p);
void piece_right(Piece *p);
void piece_rotate_clockwise(Piece *p);
void piece_rotate_counter_clockwise(Piece *p);
bool piece_equals(Piece *p1, Piece *p2);

/** Board functions */

/** Find lines that has been completed.  
 *  Returns NULL if none found. 
 */
Board * board_create(int height, int width);
void board_free (Board * b);
int ** find_completed_lines(Board * b);


#endif /* PIECES_H */

