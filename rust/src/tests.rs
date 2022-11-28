mod tests {
    use crate::{Board, Event};

    #[test]
    fn test_board1() {
        Board::new(10, 30);
    }

    #[test]
    fn test_board2() {
        let b = Board::new(10, 30);
        assert_eq!(true, b.does_piece_fit(&b.active_piece));
    }

    #[test]
    fn test_board3() {
        let b = Board::new(10, 30);
        assert_eq!(false, b.is_piece_on_bottom());
    }

    #[test]
    fn test_move_down1() {
        let mut b = Board::new(10, 30);
        let y1 = b.active_piece.blocks.first().unwrap().point.y;
        b.apply(&Event::MoveDown);
        let y2 = b.active_piece.blocks.first().unwrap().point.y;
        assert_eq!(y2, y1 + 1);
    }
}
