from blocks import *
import random
import unittest


class TestPiece(unittest.TestCase):

    def setUp(self):
        self.piece = Piece.line(1, 2)

    def test_down(self):
        self.assertEqual(self.piece.down().center.y, 3)

    def test_left(self):
        self.assertEqual(self.piece.left().center.x, 0)

    def test_right(self):
        self.assertEqual(self.piece.right().center.x, 2)

    def test_create_all(self):
        for f in Piece.creators():
            self.assertIsNotNone(f(1,1))
        self.assertIsNotNone(Piece.random_piece(1, 1))

    def test_rotate(self):
        blocks = self.piece.rotate_clockwise().blocks 
        coords = [[b.x, b.y] for b in blocks]
        self.assertEqual(coords, [[2, 2], [1, 2], [0, 2], [-1, 2]])


class TestBoard(unittest.TestCase):

    def setUp(self):
        self.board = Board(10, 10)

    def test_block_at(self):
        self.board._blocks.append(Block(1, 1, "red"))
        self.assertIsNotNone(self.board.block_at(1, 1))
        self.assertIsNone(self.board.block_at(2, 1))
        
    def test_row(self):
        for x in range(0, 4):
            self.board._blocks.append(Block(x, 1, "red"))
        self.assertEqual(10, len(self.board.row(1)))

    def test_find_complete_rows(self):
        for x in range(0, 10):
            self.board._blocks.append(Block(x, 1, "red"))
        self.assertEqual(1, len(self.board.find_complete_rows()))
        self.assertFalse(self.board.is_row_complete(2))
        self.assertTrue(self.board.is_row_complete(1))

    def test_is_block_valid(self):
        self.assertTrue(self.board.is_block_valid(Block(1, 1, "red")))
        self.assertFalse(self.board.is_block_valid(Block(-1, 1, "red")))

    def test_place_piece(self):
        self.board.place_piece(Piece.line(3, 3))
        with self.assertRaises(ValueError):
            self.board.place_piece(Piece.line(0, 0))

    def test_remove_row(self):
        for x in range(0, 5):
            self.board._blocks.append(Block(x, 5, "red"))
        for x in range(0, 10):
            self.board._blocks.append(Block(x, 6, "blue"))
        for x in range(4, 10):
            self.board._blocks.append(Block(x, 7, "green"))
        self.board.remove_row(6)
        self.assertIsNotNone(self.board.block_at(7,7))
        self.assertIsNotNone(self.board.block_at(3,6))
        self.assertIsNone(self.board.block_at(3,5))

    def test_current_piece_down(self):
        self.assertEqual(0, len(self.board._blocks))
        self.board.set_current_piece(Piece.line(1, 0).rotate_clockwise())
        for x in range(0, 10):
            self.board.push_current_piece_down();        
        self.assertEqual(4, len(self.board._blocks))
        self.assertTrue(self.board.current_piece().center.y <= 4)        

if __name__ == '__main__':
    unittest.main()

