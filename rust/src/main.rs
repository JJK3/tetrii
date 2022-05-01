extern crate core;

use rand::seq::SliceRandom;

use Event::{MoveDown, MoveLeft, MoveRight, NewGame, NewPiece, Rotate};

#[derive(Clone, Copy)]
struct Point {
    x: i16,
    y: i16,
}

trait Movable {
    fn up(p: &Point) -> Point { Point { x: p.x, y: p.y - 1 } }
    fn down(p: &Point) -> Point { Point { x: p.x, y: p.y + 1 } }
    fn left(p: &Point) -> Point { Point { x: p.x - 1, y: p.y } }
    fn right(p: &Point) -> Point { Point { x: p.x + 1, y: p.y } }
    fn rotate_around(p: &Point, other: &Point) -> Point {
        Point {
            x: (other.x + (other.y - p.y)),
            y: other.y + (p.x - other.x),
        }
    }

    fn apply(&self, func: impl Fn(&Point) -> Point) -> Box<Self>;
}


#[derive(Clone, Copy)]
enum Color {
    Cyan,
    Magenta,
    Red,
    Blue,
    Orange,
    Yellow,
    Green,
}

#[derive(Clone, Copy)]
struct Block {
    point: Point,
    color: Color,
}

impl Block {}

impl Movable for Block {
    fn apply(&self, func: impl Fn(&Point) -> Point) -> Box<Self> {
        Box::new(Block { color: self.color, point: func(&self.point) })
    }
}


#[derive(Clone, Copy)]
struct Piece {
    blocks: [Block; 4],
}

impl Piece {
    fn rotate(&self) -> impl Fn(&Point) -> Point {
        let center = self.blocks[1].point;
        move |p1: &Point| Piece::rotate_around(p1, &center)
    }


    fn line() -> Piece { Piece::new([[0, -1], [0, 0], [0, 1], [0, 2]], Color::Cyan) }
    fn n1_shape() -> Piece { Piece::new([[-1, 0], [0, 0], [0, 1], [1, 1]], Color::Yellow) }
    fn n2_shape() -> Piece { Piece::new([[-1, 0], [0, 0], [0, -1], [1, -1]], Color::Magenta) }
    fn l1_shape() -> Piece { Piece::new([[-1, 0], [0, 0], [1, 0], [1, 1]], Color::Green) }
    fn l2_shape() -> Piece { Piece::new([[-1, 0], [0, 0], [1, 0], [1, -1]], Color::Orange) }
    fn t1_shape() -> Piece { Piece::new([[-1, 0], [0, 0], [0, 1], [1, 0]], Color::Blue) }
    fn square_shape() -> Piece { Piece::new([[0, 0], [1, 0], [1, 1], [0, 1]], Color::Red) }

    fn new(points: [[i16; 2]; 4], color: Color) -> Piece {
        Piece { blocks: points.map(|p| Block { point: Point { x: p[0], y: p[1] }, color }) }
    }

    fn random() -> Piece {
        let functions = [Piece::line, Piece::n1_shape, Piece::n2_shape, Piece::l1_shape,
            Piece::l2_shape, Piece::t1_shape, Piece::square_shape];
        functions.choose(&mut rand::thread_rng()).unwrap()()
    }
}

impl Movable for Piece {
    fn apply(&self, func: impl Fn(&Point) -> Point) -> Box<Self> {
        Box::new(Piece { blocks: self.blocks.map(|b| *b.apply(&func)) })
    }
}

struct Board {
    width: i16,
    height: i16,
    active_piece: Piece,
    blocks: Vec<Block>,
    score: u32,
    is_game_over: bool,
}

impl Board {
    fn new(width: i16, height: i16) -> Self {
        Board { width, height, active_piece: Board::new_random_piece(width), blocks: vec![], score: 0, is_game_over: false }
    }

    fn assert_game_not_done(&self) {
        if self.is_game_over {
            panic!("Board cannot be modified after game is over")
        }
    }

    /** Move a piece, if possible */
    fn move_piece(&mut self, func: impl Fn(&Point) -> Point) {
        self.assert_game_not_done();
        let next_position = *self.active_piece.apply(func);
        if self.does_piece_fit(&next_position) {
            self.active_piece = next_position
        }
    }

    /** Place a piece and return whether the game is over or not */
    fn place_piece(&mut self) {
        self.assert_game_not_done();
        for b in self.active_piece.blocks {
            self.blocks.push(b)
        }

        let complete_rows: Vec<i16> = (0..self.height).filter(|y| self.is_row_complete(*y)).collect();
        if !complete_rows.is_empty() {
            self.score += Self::get_points(complete_rows.len());
            self.remove_and_shift_rows_down(complete_rows);
        }

        self.active_piece = self.new_random_piece(self.width);
        if self.does_piece_fit(&self.active_piece) {
            self.is_game_over = true
        }
    }

    /** Create a new random piece at the top of the board */
    fn new_random_piece(width: i16) -> Piece {
        *Piece::random().apply(|p| Point { x: p.x + (width / 2) - 1, y: p.y + 1 })
    }

    /** Remove complete rows and shift other nodes down */
    fn remove_and_shift_rows_down(&mut self, complete_rows: Vec<i16>) {
        self.assert_game_not_done();
        self.blocks = self.blocks.iter()
            .filter(|b| complete_rows.contains(&b.point.y))// Remove the row
            .map(|b|
                // For every completed row, shift blocks down
                complete_rows.iter()
                    .filter(|r| b.point.y < **r)
                    .fold(*b, |it, _| *it.apply(Piece::down))
            ).collect();
    }

    fn is_piece_on_bottom(&self) -> bool {
        self.does_piece_fit(&self.active_piece.apply(Piece::down))
    }

    /** How many points do you get for completing 1 row? 2 rows etc. */
    fn get_points(rows: usize) -> u32 {
        match rows {
            1 => 10,
            2 => 25,
            3 => 40,
            4 => 55,
            _ => 0
        }
    }

    fn is_row_complete(&self, y: i16) -> bool {
        (0..self.width).all(|x| self.block_at(&Point { x, y }).is_some())
    }

    /** Does the given piece fit on the board? */
    fn does_piece_fit(&self, piece: &Piece) -> bool {
        piece.blocks.iter().all(|b|
            (0..self.width).contains(&b.point.x) &&
                (0..self.height).contains(&b.point.y) &&
                self.block_at(&b.point).is_none()
        )
    }

    /** Get the block at the given point */
    fn block_at(&self, p: &Point) -> Option<&Block> {
        self.blocks.iter().find(|b| b.point.x == p.x && b.point.y == p.y)
    }

    fn apply(&mut self, event: &Event) {
        self.assert_game_not_done();
        match event.clone() {
            NewGame(w, h) => {
                self.width = w;
                self.height = h;
            }
            NewPiece(p) => self.active_piece = p,
            MoveLeft() => self.move_piece(Piece::left),
            MoveRight() => self.move_piece(Piece::right),
            MoveDown() => {
                self.move_piece(Piece::down);
                if self.is_piece_on_bottom() {
                    self.place_piece();
                }
            }
            Rotate() => self.move_piece(self.active_piece.rotate())
        }
    }
}

#[derive(Clone, Copy)]
enum Event {
    NewGame(i16, i16),
    NewPiece(Piece),
    MoveLeft(),
    MoveRight(),
    MoveDown(),
    Rotate(),
}

struct EventLog {
    events: Vec<Event>,
}

impl EventLog {
    fn plus(&mut self, event: Event) {
        self.events.push(event);
    }
}

mod tests {
    use crate::Board;

    #[test]
    fn test_board1() {
        let x = Board::new(10,30);
        x.apply()
    }
}

fn main() {
    println!("Hello, world!");
}
