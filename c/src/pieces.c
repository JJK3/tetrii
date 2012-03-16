#include <config.h>
#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "pieces.h"

const char * COLORS[5] = {"#BB0000", "blue", "#2dd400", "#ff950c", "#2ea4ff"};

/** Point functions */
bool point_equals(Point *p1, Point *p2)
{
	return p1 != NULL && p2 != NULL &&
		p1->x == p2->x && p1->y == p2->y;
};

void point_print(Point *p){
	printf("Point(%i, %i)\n", p->x, p->y);
};

Point * point_create (int x, int y)
{
	Point *p = malloc (sizeof (Point));
	p->x = x;
	p->y = y;
	return p;
};

Point * point_copy (Point * old_point)
{
	Point *p = malloc (sizeof (Point));
	p->x = old_point->x;
	p->y = old_point->y;
	p->color = old_point->color;
	return p;
};

void point_free (Point * p)
{
	free (p);
	return;
};



/** Piece functions */

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
	return piece_create(x, y, blocks, "blue");
};

/**
 * Square shape
 *  ##
 *  ##
 */
Piece * square(int x, int y)
{
	int blocks[4][2] = {{0,0}, {1,0}, {1,1}, {0,1}};
	return piece_create(x, y, blocks, "#BB0000");
};

/**
 * L-shape 1
 *  ###
 *    #
 */
Piece * l_shape1(int x, int y)
{
	int blocks[4][2] = {{-1,0}, {0,0}, {1,0}, {1,1}};
	return piece_create(x, y, blocks, "#2dd400");
};

/**
 * L-shape 2
 *    #
 *  ###
 */
Piece * l_shape2(int x, int y)
{
	int blocks[4][2] = {{-1,0}, {0,0}, {1,0}, {1,-1}};
	return piece_create(x, y, blocks, "#ff950c");
};

/**
 * N-shape 1
 *  ##
 *   ##
 */
Piece * n_shape1(int x, int y)
{
	int blocks[4][2] = {{-1,0}, {0,0}, {0,1}, {1,1}};
	return piece_create(x, y, blocks, "#2ea4ff");
};

/**
 * N-shape 2
 *   ##
 *  ##
 */
Piece * n_shape2(int x, int y)
{
	int blocks[4][2] = {{-1,0}, {0,0}, {0,-1}, {1,-1}};
	return piece_create(x, y, blocks, "#4b0063");
};

Piece * piece_create_random(int x, int y)
{
	Piece * (* const func[6]) (int x, int y) = {
		line, square, l_shape1, l_shape2, n_shape1, n_shape1
	};
	int i = rand() % 6;
	Piece * p = (*func[i])(x, y);
	return p;
}

/** Piece constructor */
Piece * piece_create(int center_x, int center_y, int coords[4][2], char * color)
{
	Point ** blocks = (Point **) malloc(sizeof(Point)*4);
	for (int i=0; i<4; i++){
		Point * new_point = point_create(coords[i][0], coords[i][1]);
		blocks[i] = new_point;
		new_point->color = color;
	}
	Point * center = point_create(center_x, center_y);
	Piece * p = malloc (sizeof(Piece));
	p->center = center;
	p->blocks = blocks;
	return p;
};

Piece * piece_copy(Piece * old_piece)
{
	Point ** blocks = (Point **) malloc(sizeof(Point)*4);
	for (int i=0; i<4; i++) {
		blocks[i] = point_copy(old_piece->blocks[i]);
	}
	Point * center = point_create(old_piece->center->x, old_piece->center->y);
	Piece * p = malloc (sizeof(Piece));
	p->center = center;
	p->blocks = blocks;
	return p;
}

void piece_free (Piece * p)
{
	free(p->center);
	free(p->blocks);
	free (p);
	return;
};

/* Mutate a piece by moving down 1 */
void piece_down(Piece* p)
{
	p->center->y++;
};

/* Mutate a piece by moving left 1 */
void piece_left(Piece *p)
{
	p->center->x--;
};

/* Mutate a piece by moving right 1 */
void piece_right(Piece *p)
{
	p->center->x++;
};

/* Mutate a piece by rotating it clockwise around (0,0) */
void piece_rotate_clockwise(Piece *p)
{
	for (int i=0; i<4; i++) {
		Point * point = p->blocks[i];
		int x = point->x;
		point->x = -(point->y);
		point->y = x;
	}
};

/* Mutate a piece by rotating it counter clockwise around (0,0) */
void piece_rotate_counter_clockwise(Piece *p)
{
	for (int i=0; i<4; i++) {
		Point * point = p->blocks[i];
		int x = point->x;
		point->x = point->y;
		point->y = -(x);
	}
};

bool piece_equals(Piece *p1, Piece *p2)
{
	if (p1 == NULL || p2 == NULL) {
		return false; 
	}
	if (!point_equals((p1->center), (p2->center))) {
		return false; 
	}

	for (int i=0; i<4; i++) {
		Point *point1 = p1->blocks[i];
		Point *point2 = p2->blocks[i];
		if (!point_equals(point1, point2)){ 
			return false;
		}
	}
	return true;
};






/** Board functions */
Board * board_create()
{
	Board *b = malloc (sizeof (Board));
	b->height = HEIGHT;
	b->width = WIDTH;
	b->score = 0;
	b->is_done = false;
	//  b->placed_blocks = p;
	b->current_piece = piece_create_random((b->width / 2), 1);
	for (int x=0; x<b->width; x++){
		for (int y=0; y<b->height; y++){
			b->placed_blocks[x][y] = NULL;
		}
	}
	return b;
};

void board_free (Board * b)
{
	free (b->current_piece);
	free (b->placed_blocks);
	free (b);
	return;
};

/* Get the piece at the given x,y coords */
Point * board_find_piece_at(Board * b, int x, int y)
{
	return b->placed_blocks[x][y];
};

/** Is the given row complete? */
bool board_is_row_complete(Board * b, int row)
{
	for (int x=0; x<b->width; x++) {
		Point * found_piece = board_find_piece_at(b, x, row);
		if (found_piece == NULL) {
			return false;
		}
	}
	return true;
};

/** 
 * Find completed rows on the board.
 * This function will return an array of ints representing the completed
 * rows
 * i.e. [0,0,1,1,0,0,1,0,0]. 
 */
bool * board_find_completed_rows(Board * b)
{
	bool * result = malloc(sizeof(bool) * b->height);
	for (int y=0; y<b->height; y++)	{
		result[y] = board_is_row_complete(b, y);
	}
	return result;
};

/** Count the number of true values in the array. */
int count_true(bool * boolean_list, int list_size)
{
	int count = 0;
	for (int i=0; i<list_size; i++) {
		if (boolean_list[i]) {
			count++;
		}
	}
	return count;
}

/** 
 * Is the given piece at valid coordinates? I
 * s it within bounds and not overlapping any other pieces? 
 */
bool board_check_valid_placement(Board * b, Piece * p)
{
	for (int i=0; i<4; i++){
		Point * current_point = p->blocks[i];
		int absolute_x = current_point->x + p->center->x; 
		int absolute_y = current_point->y + p->center->y; 
		if (absolute_x < 0 || absolute_x >= b->width || absolute_y >= b->height) {
			return false;
		}

		Point * overlapping_point = board_find_piece_at(b, absolute_x, absolute_y);
		if (overlapping_point != NULL) {
			return false;
		}
	}
	return true;
}

/** 
 * Is is possible for the given piece to move down? 
 * Or is touching the bottom?.
 */  
bool board_can_piece_move_down(Board * b)
{
	Piece * copy = piece_copy(b->current_piece);
	piece_down(copy);
	bool result = board_check_valid_placement(b, copy);
	piece_free(copy);
	return result;
}

/** Add a piece to the board. */
void board_place_piece(Board * b, Piece * p)
{
	//  assert(board_check_valid_placement(b, p));
	for (int i=0; i<4; i++)	{
		Point * point = p->blocks[i];
		int absolute_x = point->x + p->center->x; 
		int absolute_y = point->y + p->center->y; 
		Point * new_point = point_create(absolute_x, absolute_y);
		new_point->color = point->color;
		b->placed_blocks[absolute_x][absolute_y] = new_point;
	}
}

/** Remove a row from the board and push the remaining blocks down. */
void board_remove_row(Board * b, int row)
{
	// Remove the row;
	for (int x=0; x<b->width; x++) {
		Point * p = b->placed_blocks[x][row];
		point_free(p);
		b->placed_blocks[x][row] = NULL;
	}  

	// Move the other blocks down
	for (int y=row; y>0; y--) {
		for (int x=0; x<b->width; x++) {
			Point * above = b->placed_blocks[x][y-1];
			b->placed_blocks[x][y] = above;
			if (above != NULL){
				above->y = y;
			}
			b->placed_blocks[x][y-1] = NULL;
		}
	}
}

void board_print(Board * b)
{
	for (int y=0; y<b->height; y++) {
		for (int x=0; x<b->width; x++) {	
			Point * p = b->placed_blocks[x][y];
			if (p != NULL){
				printf("X ");
			} else {
				printf(". ");
			}
		}
		printf("\n");
	}
	printf("\n");
}

/** Attempts to push the current piece down */
bool board_push_current_piece_down(Board * b)
{
	// First test if the current piece is already touching something.
	// This can happen if you move sideways and are now touching another piece.
	bool is_on_bottom = !board_can_piece_move_down(b);
	bool result = false;
	if (!is_on_bottom) {
		piece_down(b->current_piece);
		is_on_bottom = !board_can_piece_move_down(b);
		result = true;
	}
	
	if (is_on_bottom){
		// remove the rows
		board_place_piece(b, b->current_piece);
		bool * completed_rows = board_find_completed_rows(b);
		int total_complete_rows = count_true(completed_rows, b->height);
		for (int y=b->height-1; y>=0; y--) {
			if (completed_rows[y]) {
				board_remove_row(b, y);
			}
		}
		free(completed_rows);

		// score the points
		if (total_complete_rows == 1) {
			b->score += 10;
		} else if (total_complete_rows == 2) {
			b->score += 25;
		} else if (total_complete_rows == 3) {
			b->score += 40;
		} else if (total_complete_rows == 4) {
			b->score += 55;
		}
		if (total_complete_rows > 0){
			printf("Score is %i\n", b->score);
		}

		//		board_print(b);
		Piece * next_piece = piece_create_random((b->width / 2), 1);
		if (board_check_valid_placement(b, next_piece)){
			piece_free(b->current_piece);
			b->current_piece = next_piece;						
		} else {
			piece_free(next_piece);
			b->is_done = true;
		}		
	}
	return result;
}
