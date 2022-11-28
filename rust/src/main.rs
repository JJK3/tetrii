mod tests;
mod tetris_core;

use std::borrow::Borrow;
use ncurses::*;
use rand::seq::SliceRandom;
use std::thread;
use std::thread::sleep;
use std::time::Duration;
use std::sync::{Arc, Mutex};
use ncurses::CURSOR_VISIBILITY::CURSOR_INVISIBLE;
use Event::*;

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
        [
            Piece::line,
            Piece::n1_shape,
            Piece::n2_shape,
            Piece::l1_shape,
            Piece::l2_shape,
            Piece::t1_shape,
            Piece::square_shape
        ].choose(&mut rand::thread_rng()).unwrap()()
    }

    /** Create a new random piece at the top of the board */
    fn new_random_piece(width: i16) -> Piece {
        *Piece::random().apply(|p| Point { x: p.x + (width / 2) - 1, y: p.y + 1 })
    }
}

impl Movable for Piece {
    fn apply(&self, func: impl Fn(&Point) -> Point) -> Box<Self> {
        return Box::new(Piece { blocks: self.blocks.map(|b| *b.apply(&func)) });
    }
}

#[derive(Clone)]
struct Board {
    width: i16,
    height: i16,
    active_piece: Piece,
    blocks: Vec<Block>,
    score: u32,
    is_game_over: bool,
    log: Vec<Event>,
}

impl Board {
    fn new(width: i16, height: i16) -> Self {
        let piece = Piece::new_random_piece(width);
        Board {
            width,
            height,
            active_piece: piece,
            blocks: vec![],
            score: 0,
            is_game_over: false,
            log: vec![
                NewGame(width, height),
                NewPiece(piece),
            ],
        }
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
            self.active_piece = next_position;
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

        self.active_piece = Piece::new_random_piece(self.width);
        if !self.does_piece_fit(&self.active_piece) {
            self.is_game_over = true;
        }
    }

    /** Remove complete rows and shift other nodes down */
    fn remove_and_shift_rows_down(&mut self, complete_rows: Vec<i16>) {
        self.assert_game_not_done();
        self.blocks = self.blocks.iter()
            .filter(|b| !complete_rows.contains(&b.point.y))// Remove the row
            .map(|b|
                // For every completed row, shift blocks down
                complete_rows.iter()
                    .filter(|r| b.point.y < **r)
                    .fold(*b, |it, _| *it.apply(Piece::down))
            ).collect();
    }

    fn is_piece_on_bottom(&self) -> bool {
        !self.does_piece_fit(&self.active_piece.apply(Piece::down))
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
        let e = event.clone();
        match e {
            NewGame(_, _) => panic!("Game is already started"),
            NewPiece(p) => self.active_piece = p,
            MoveLeft => self.move_piece(Piece::left),
            MoveRight => self.move_piece(Piece::right),
            MoveDown => {
                self.move_piece(Piece::down);
                if self.is_piece_on_bottom() {
                    self.place_piece();
                }
            }
            MoveAllTheWayDown => {
                while !self.is_piece_on_bottom() {
                    self.move_piece(Piece::down);
                }
                self.place_piece();
            }
            Rotate => self.move_piece(self.active_piece.rotate())
        }
        self.log.push(e)
    }
}

#[derive(Clone, Copy)]
enum Event {
    NewGame(i16, i16),
    NewPiece(Piece),
    MoveLeft,
    MoveRight,
    MoveDown,
    MoveAllTheWayDown,
    Rotate,
}

struct EventLog {
    events: Vec<Event>,
}

impl EventLog {
    fn plus(&mut self, event: Event) {
        self.events.push(event);
    }
}

struct Game {
    window: Foo,
    board: Board,
}

impl Game {
    pub fn new() -> Self {
        Game {
            window: Foo { window: initscr() },
            board: Board::new(10, 20),
        }
    }

    fn curses_color(c: Color) -> (i16, i16, i16, chtype) {
        return match c {
            Color::Cyan => (1, COLOR_CYAN, COLOR_BLACK, '#' as chtype),
            Color::Magenta => (2, COLOR_MAGENTA, COLOR_BLACK, '#' as chtype),
            Color::Red => (3, COLOR_RED, COLOR_BLACK, '#' as chtype),
            Color::Blue => (4, COLOR_BLUE, COLOR_BLACK, '#' as chtype),
            Color::Orange => (5, COLOR_RED, COLOR_YELLOW, '#' as chtype),
            Color::Yellow => (6, COLOR_YELLOW, COLOR_BLACK, '#' as chtype),
            Color::Green => (7, COLOR_GREEN, COLOR_BLACK, '#' as chtype),
        };
    }

    fn draw_block(&self, block: &Block) {
        let w = &self.window.window;
        wmove(*w, block.point.y as i32 + 1, block.point.x as i32 + 1);
        let color = Game::curses_color(block.color);
        wcolor_set(*w, color.0);
        addch(color.3);
    }

    fn draw(&self) {
        let board = &self.board;
        clear();
        wcolor_set(self.window.window, 99);
        wmove(self.window.window, 1, 30);
        waddstr(self.window.window, format!("Score: {}", self.board.score).borrow());
        mvhline(0, 1, '-' as chtype, board.width as i32);
        mvvline(1, 0, '|' as chtype, board.height as i32);
        mvvline(1, board.width as i32 + 1, '|' as chtype, board.height as i32 );
        mvhline(board.height as i32 + 1, 1, '-' as chtype, board.width as i32 );
        for block in board.active_piece.blocks {
            self.draw_block(&block)
        }
        for block in board.blocks.clone() {
            self.draw_block(&block)
        }
    }

    fn event(&mut self, e: &Event) {
        self.board.apply(e);
        self.draw();
        refresh();
    }

    fn play() {
        let game = Game::new();
        start_color();
        curs_set(CURSOR_INVISIBLE);
        let all_colors = vec![
            Color::Cyan,
            Color::Magenta,
            Color::Red,
            Color::Blue,
            Color::Orange,
            Color::Yellow,
            Color::Green,
        ];
        init_pair(99, COLOR_WHITE, COLOR_BLACK);
        for c in all_colors {
            let cc = Game::curses_color(c);
            init_pair(cc.0, cc.1, cc.2);
        }
        let board = Arc::new(Mutex::new(game));
        let b = Arc::clone(&board);
        let b2 = Arc::clone(&board);
        thread::spawn(move || {
            while !b.clone().lock().unwrap().board.is_game_over {
                sleep(Duration::from_millis(1000));
                b.clone().lock().unwrap().event(&Event::MoveDown);
            }
        });
        let mut ch = getch();
        while ch != 'q' as i32 && !b2.lock().unwrap().board.is_game_over {
            ch = getch();
            let mut guard = b2.lock().unwrap();
            if !guard.board.is_game_over {
                if ch == 65 {
                    guard.event(&Event::Rotate);
                } else if ch == 68 {
                    guard.event(&Event::MoveLeft);
                } else if ch == 67 {
                    guard.event(&Event::MoveRight);
                } else if ch == 66 {
                    guard.event(&Event::MoveDown);
                } else if ch == 32 {
                    guard.event(&Event::MoveAllTheWayDown);
                }
            }
        }
        endwin();
        println!("Game over! Your score: {}", b2.lock().unwrap().board.score);
    }
}


struct Foo {
    window: *mut i8,
}

unsafe impl Send for Foo {}

fn main() {
    Game::play();
}
