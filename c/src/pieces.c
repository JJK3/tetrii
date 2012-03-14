#include <config.h>
#include <stdio.h>
#include <stdlib.h>
#include "pieces.h"

/** A Simple 2-D Point */
bool point_equals(Point *p1, Point *p2)
{
  return p1 != NULL && p2 != NULL &&
    p1->x == p2->x && p1->y == p2->y;
}
void point_print(Point *p){
	printf("Point(%i, %i)\n", p->x, p->y);
}

Point * point_create (int x, int y)
{
  Point *p = malloc (sizeof (Point));
  p->x = x;
  p->y = y;
  return p;
}

void point_free (Point * p)
{
  free (p);
  return;
}




/**
 * Line shape
 *   #
 *   #
 *   #
 *   #
 */
Piece * line(int x, int y)
{
  int blocks[4][2] = {{0,-1}, {0,0}, {0,1}, {0,2}};
  return piece_create(x, y, blocks);
};

/**
 * Square shape
 *  ##
 *  ##
 */
Piece * square(int x, int y)
{
  int blocks[4][2] = {{0,0}, {1,0}, {1,1}, {0,1}};
  return piece_create(x, y, blocks);
};

/**
 * L-shape 1
 *  ###
 *    #
 */
Piece * l_shape1(int x, int y)
{
  int blocks[4][2] = {{-1,0}, {0,0}, {1,0}, {1,1}};
  return piece_create(x, y, blocks);
};

/**
 * L-shape 2
 *    #
 *  ###
 */
Piece * l_shape2(int x, int y)
{
  int blocks[4][2] = {{-1,0}, {0,0}, {1,0}, {1,-1}};
  return piece_create(x, y, blocks);
};

/**
 * N-shape 1
 *  ##
 *   ##
 */
Piece * n_shape1(int x, int y)
{
  int blocks[4][2] = {{-1,0}, {0,0}, {0,1}, {1,1}};
  return piece_create(x, y, blocks);
};

/**
 * N-shape 2
 *   ##
 *  ##
 */
Piece * n_shape2(int x, int y)
{
  int blocks[4][2] = {{-1,0}, {0,0}, {0,-1}, {1,-1}};
  return piece_create(x, y, blocks);
};

/** Piece constructor */
Piece * piece_create(int center_x, int center_y, int coords[4][2])
{
  Point ** blocks = (Point **) malloc(sizeof(Point)*4);
  blocks[0] = point_create(coords[0][0], coords[0][1]);
  blocks[1] = point_create(coords[1][0], coords[1][1]);
  blocks[2] = point_create(coords[2][0], coords[2][1]);
  blocks[3] = point_create(coords[3][0], coords[3][1]);
  Point * center = point_create(center_x, center_y);
  Piece * p = malloc (sizeof(Piece));
  p->center = center;
  p->blocks = blocks;
  return p;
};

void piece_free (Piece * p)
{
  free(p->center);
  free(p->blocks);
  free (p);
  return;
}

/* Mutate a piece by moving down 1 */
void piece_down(Piece* p)
{
  p->center->y++;
}

/* Mutate a piece by moving left 1 */
void piece_left(Piece *p)
{
  p->center->x--;
}

/* Mutate a piece by moving right 1 */
void piece_right(Piece *p)
{
  p->center->x++;
}

/* Mutate a piece by rotating it clockwise around (0,0) */
void piece_rotate_clockwise(Piece *p)
{
	int i;
	for (i=0; i<4; i++){
		Point * point = p->blocks[i];
		int x = point->x;
		point->x = -(point->y);
		point->y = x;
	}
}

/* Mutate a piece by rotating it counter clockwise around (0,0) */
void piece_rotate_counter_clockwise(Piece *p)
{
	int i;
	for (i=0; i<4; i++){
		Point * point = p->blocks[i];
		int x = point->x;
		point->x = point->y;
		point->y = -(x);
	}
}

/** Test whether the given point exists in this piece. */
bool piece_contains_point(Piece * piece, Point * test_point){
    Point * relative_point = point_create(test_point->x - piece->center->x,
									      test_point->y - piece->center->y);
  	int i;
  	for (i=0; i<4; i++)
  	{
  		Point * current_point = piece->blocks[i];
  		if (point_equals(current_point, relative_point))
  		{
  			return true;
  		}
  	}
  	return false;
}

bool piece_equals(Piece *p1, Piece *p2)
{
  if (p1 == NULL || p2 == NULL)
    return false; 
  if (!point_equals((p1->center), (p2->center)))
    return false; 

  int i;
  for (i=0; i<4; i++){
    Point *point1 = p1->blocks[i];
    Point *point2 = p2->blocks[i];
    if (!point_equals(point1, point2)){ 
      //printf("asd %i", point1);
      return false;
    }
  }
  return true;
}

/** A linked list of pieces. */
LinkedList * linked_list_create()
{
	LinkedList *pl = malloc (sizeof (LinkedList));
	pl->car = NULL;
	pl->cdr = NULL;
	return pl;
}

void linked_list_free(LinkedList * pl)
{
	free(pl->car);
	if (pl->cdr != NULL)
	{
		linked_list_free(pl->cdr);
	}
	free(pl);
}

LinkedList * linked_list_cons(LinkedList * list, void * element)
{
  LinkedList * new_list = linked_list_create();
  new_list->car = element;
  new_list->cdr = list;
  return new_list;  
}

void linked_list_iterate(LinkedList * list, void (*iterator) (void *))
{
	if (list != NULL)
	{
		LinkedList * current_list = list;
		void * current = list->car;
		while (current != NULL){
			(*iterator)(current);	
			current_list = current_list->cdr;
			current = current_list->car;
		}
	}
}







/** Board functions */
Board * board_create(int width, int height)
{
  Board *b = malloc (sizeof (Board));
  b->height = height;
  b->width = width;
  b->placed_pieces = linked_list_create();
  b->current_piece = NULL;
  return b;
}

void board_free (Board * b)
{
  free (b->current_piece);
  free (b->placed_pieces);
  free (b);
  return;
}

Piece * board_find_piece_at(Board * b, int x, int y)
{
  Point * test_point = point_create(x, y);
  Piece * found_piece = NULL;
  void it (Piece * piece){
	if (piece_contains_point(piece, test_point))
	{
		found_piece = piece;
	}  	
  };
  linked_list_iterate(b->placed_pieces, &it);
  return found_piece;
}



int ** find_completed_lines(Board * b)
{
  int x;
  int y;
  for (y=0; y<b->height; y++)
  {
	  bool is_complete = true;
	  for (x=0; x<b->width; x++)
	  {
	  	Piece * found_piece = board_find_piece_at(b, x, y);
		if (found_piece == NULL)
		{
			is_complete = false;
			//break;			
		}	  	 
	  }  
	  if (is_complete){
		  //TODO: add it to the list.
		  printf("Found a COMPLETE row: %i\n", y);
	  } else {
  		  printf("Found an INcomplete row: %i\n", y);
	  }
  }
 return NULL;
}

