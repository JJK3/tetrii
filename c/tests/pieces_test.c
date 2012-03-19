#include </usr/include/check.h>
#include <stdlib.h>
#include <stdio.h>
#include "../src/pieces.h"



START_TEST (point_tests)
{
	Point * p1 = point_create(-1,0);
	Point * p2 = point_create(0, 0);
	fail_unless (point_equals(p1, p1), "the same point was not equal");
	fail_if (point_equals(p1, p2), "different points should not be equal");
	point_free(p1);
	point_free(p2);
}
END_TEST


START_TEST (piece_test_equals)
{
	Piece * p1 = line(0,0);
	Piece * p2 = line(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);
	
	p1 = square(0,0);
	p2 = square(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);

	p1 = l_shape1(0,0);
	p2 = l_shape1(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);

	p1 = l_shape2(0,0);
	p2 = l_shape2(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);

	p1 = n_shape1(0,0);
	p2 = n_shape1(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);

	p1 = n_shape2(0,0);
	p2 = n_shape2(1,0);
	fail_unless (piece_equals(p1, p1), "the same piece was not equal");
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_free(p1);
	piece_free(p2);

}
END_TEST

START_TEST (piece_test_move)
{
	Board * board = board_create();
	Piece * p1 = l_shape1(3,3);
	Piece * p2 = l_shape1(3,3);
	fail_unless (piece_equals(p1, p2), "the same piece was not equal");
	piece_left(p1);
	fail_if (piece_equals(p1, p2), "different pieces should not be equal");
	piece_right(p1);
	fail_unless (piece_equals(p1, p2), "pieces should be equal after move");

	piece_free(p1);
	piece_free(p2);
}
END_TEST

START_TEST (piece_test_rotate_clockwise)
{
	Piece * original = l_shape1(3,3);
	Piece * p1 = l_shape1(3,3);

	/* Rotate one time. */
	int blocks[4][2] = {{0,-1}, {0,0}, {0,1}, {-1,1}};
	Piece * rotate_1_time = piece_create(3, 3, blocks, NULL);
	piece_rotate_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_1_time), "pieces should be equal after 1 rotation");
	
	/* Rotate a second time. */
	int blocks2[4][2] = {{1,0}, {0,0}, {-1,0}, {-1,-1}};
	Piece * rotate_2_time = piece_create(3, 3, blocks2, NULL);
	piece_rotate_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_2_time), "pieces should be equal after 2 rotations");
	
	/* Rotate a third time. */
	int blocks3[4][2] = {{0,1}, {0,0}, {0,-1}, {1,-1}};
	Piece * rotate_3_time = piece_create(3, 3, blocks3, NULL);
	piece_rotate_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_3_time), "pieces should be equal after 3 rotations");

	/* Rotate a fourth time. */
	piece_rotate_clockwise(p1);
	fail_unless (piece_equals(p1, original), "pieces should be equal after 4 rotations");
	
	piece_free(p1);
	piece_free(original);
	piece_free(rotate_1_time);
	piece_free(rotate_2_time);
	piece_free(rotate_3_time);
}
END_TEST

START_TEST (piece_test_rotate_counter_clockwise)
{
	Piece * original = l_shape1(3,3);
	Piece * p1 = l_shape1(3,3);

	/* Rotate one time. */
	int blocks3[4][2] = {{0,1}, {0,0}, {0,-1}, {1,-1}};
	Piece * rotate_1_time = piece_create(3, 3, blocks3, NULL);
	piece_rotate_counter_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_1_time), "pieces should be equal after 1 rotation");

	/* Rotate a second time. */
	int blocks2[4][2] = {{1,0}, {0,0}, {-1,0}, {-1,-1}};
	Piece * rotate_2_time = piece_create(3, 3, blocks2, NULL);
	piece_rotate_counter_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_2_time), "pieces should be equal after 2 rotations");

	/* Rotate a third time. */
	int blocks[4][2] = {{0,-1}, {0,0}, {0,1}, {-1,1}};
	Piece * rotate_3_time = piece_create(3, 3, blocks, NULL);
	piece_rotate_counter_clockwise(p1);
	fail_unless (piece_equals(p1, rotate_3_time), "pieces should be equal after 3 rotations");

	/* Rotate a fourth time. */
	piece_rotate_counter_clockwise(p1);
	fail_unless (piece_equals(p1, original), "pieces should be equal after 4 rotations");

	piece_free(p1);
	piece_free(original);
	piece_free(rotate_1_time);
	piece_free(rotate_2_time);
	piece_free(rotate_3_time);
}
END_TEST



START_TEST (board_test)
{
	Board * b = board_create();
	bool * completed = board_find_completed_rows(b);
	fail_unless (completed[0] == 0, "There shouldn't be any completed lines for an empty board");
	free(completed);

	for (int i=0; i<WIDTH+1; i++){
		Piece * p = line(i, 8);
		board_place_piece(b, p);
	}
	completed = board_find_completed_rows(b);
	fail_unless (completed[7], "Row 7 should be complete");
	fail_unless (completed[8], "Row 8 should be complete");
	fail_unless (completed[9], "Row 9 should be complete");
	fail_unless (completed[10], "Row 10 should be complete");
	free(completed);
}
END_TEST



START_TEST (test_random_piece)
{
	for (int i=0; i<30; i++){
		Piece * p = piece_create_random(5, 2);
		fail_if(p == NULL, "random piece shouldn't be null");
	}
}
END_TEST



START_TEST (move_piece_test)
{
	Piece * p1 = line(2,3);
	piece_down(p1);
	fail_unless (p1->center->y = 4, "Piece down should increase y1");  
	piece_down(p1);
	fail_unless (p1->center->y = 5, "Piece down should increase y2");  
	piece_down(p1);
	fail_unless (p1->center->y = 6, "Piece down should increase y3");  

	Board * b = board_create();
	Piece * p = line(2, 0);  
	b->current_piece = p;
	fail_unless (board_can_piece_move_down(b), "Piece should be able to move down. 1");  

	b = board_create();
	p = line(2, HEIGHT);  
	b->current_piece = p;
	fail_if (board_can_piece_move_down(b), "Piece should be not able to move down. 1");  

	b = board_create();
	p = line(4, 0);  
	piece_rotate_clockwise(p);
	b->current_piece = p;
	for (int i=0; i<HEIGHT; i++){
		fail_unless (board_push_current_piece_down(b), "Piece should be able to move down. ");  
	}
	fail_unless (b->current_piece->center->y == 3, "The piece should be a new piece");  
}
END_TEST





Suite *
full_suite (void)
{
	Suite *s = suite_create ("Pieces");

	/* Core test case */
	TCase *tc_core = tcase_create ("Core");
	tcase_add_test (tc_core, point_tests);
	tcase_add_test (tc_core, piece_test_equals);
	tcase_add_test (tc_core, piece_test_move);
	tcase_add_test (tc_core, piece_test_rotate_clockwise);
	tcase_add_test (tc_core, piece_test_rotate_counter_clockwise);
	tcase_add_test (tc_core, board_test);
	tcase_add_test (tc_core, move_piece_test);
	tcase_add_test (tc_core, test_random_piece);
	suite_add_tcase (s, tc_core);
	return s;
}

int
main (void)
{
	srand(time(NULL));
	int number_failed;
	Suite *s = full_suite ();
	SRunner *sr = srunner_create (s);
	srunner_run_all (sr, CK_NORMAL);
	number_failed = srunner_ntests_failed (sr);
	srunner_free (sr);
	return (number_failed == 0) ? EXIT_SUCCESS : EXIT_FAILURE;
}
