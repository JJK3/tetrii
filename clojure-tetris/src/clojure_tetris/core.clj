(ns clojure-tetris.core
  (:gen-class)
  (:import (java.awt Color Graphics Font Dimension Component)
           (javax.swing JFrame JPanel)
           (java.awt.event KeyAdapter KeyEvent)))

(defrecord Block [x y color])
(defrecord Board [blocks width height currentPiece score])

(defn down [^Block b] (update b :y inc))
(defn left [^Block b] (update b :x dec))
(defn right [^Block b] (update b :x inc))
(defn plus [x y] (fn [^Block b] (update (update b :x #(+ % x)) :y #(+ % y))))

(defn create [coords color] (map #(->Block (first %) (second %) color) coords))
(def line (create '((0 0) (0 1) (0 2) (0 3)) Color/CYAN))
(def square (create '((0 0) (1 0) (1 1) (0 1)) (Color. 0xBB0000)))
(def n1-piece (create '((-1 0) (0 0) (0 1) (1 1)) (Color. 0x3333FF)))
(def n2-piece (create '((-1 0) (0 0) (0 -1) (1 -1)) Color/MAGENTA))
(def l1-piece (create '((-1 0) (0 0) (1 0) (1 1)) (Color. 0x2dd400)))
(def l2-piece (create '((-1 0) (0 0) (1 0) (1 -1)) Color/ORANGE))
(def t-piece (create '((-1 0) (0 0) (1 0) (0 1)) Color/BLUE))

(defn blockAt "Get the block at [x,y] or nil" [board x y]
  (some #(and (= x (:x %)) (= y (:y %))) (:blocks board)))

(defn rowAt "Get the entire row of blocks. Any empty blocks will be nil" [board y]
  (map #(blockAt board % y) (range 0 (:width board))))

(defn canFit?
  "Test whether a Block or list of Blocks are all in bounds and not overlapping other blocks"
  [p board]
  (if (sequential? p)
    (every? #(canFit? % board) p)
    (let [x (:x p)
          y (:y p)]
      (and (>= x 0) (>= y 0) (< x (:width board)) (< y (:height board)) (nil? (blockAt board x y))))))

(defn forceMove [p direction]
  (if (sequential? p)
    (map #(direction %) p)
    (direction p)))

(defn move
  "Moves a Block or list of Blocks in a direction if predicate is true. direction is a function of (Point) -> Point"
  [p direction board]
  (let [moved (forceMove p direction)]
    (if (canFit? moved board)
      moved
      p)))

(defn randomPiece
  ([] (rand-nth (list line square n1-piece n2-piece l1-piece l2-piece t-piece)))
  ([board] (forceMove (randomPiece) (plus (/ (:width board) 2) 1))))

(defn rotate
  "Rotate a Block or list of Blocks around a point"
  ([blocks] (map #(rotate % (second blocks)) blocks))
  ([block point] (let [x (:x point)
                       y (:y point)]
                   (->Block (+ x (- y (:y block))) (+ y (- (:x block) x)) (:color block)))))

(defn rotateCurrentPiece [board]
  (let [rotated (rotate (:currentPiece board))]
    (if (canFit? rotated board)
      (update board :currentPiece (fn [p] rotated))
      board)))

(defn canMove?
  "Test whether a Block or list of Blocks can be moved in a direction"
  [p board direction]
  (if (sequential? p)
    (every? #(canMove? % board direction) p)
    (canFit? (direction p) board)))

(defn removeRow
  "Removes a row from the board and shifts all other blocks downward"
  [^Board board y]
  (update board :blocks (fn [blocks] (filter #(not (nil? %))
                                             (map #(cond (> (:y %) y) %
                                                         (< (:y %) y) (down %)) blocks)))))

(defn isComplete? [board y] (not-any? nil? (rowAt board y)))

(defn completeRows "Get the y coordinate of all complete rows" [board]
  (remove nil? (map #(if (isComplete? board %) % nil) (range 0 (:height board)))))

(defn score "Get the score for n complete rows" [n] (case n
                                                      1 50,
                                                      2 100,
                                                      3 300,
                                                      4 1200
                                                      0))

(defn newPiece [board] (update board :currentPiece (fn [_] (randomPiece board))))

(defn placePiece "Place the current piece on the board and get a new random piece"
  [board] (newPiece (update board :blocks #(concat % (:currentPiece board)))))

(defn removeCompleteRowsAndScore "Remove completed rows and add to the score" [board]
  (let [complete (completeRows board)
        board (update board :score #(+ % (score (count complete))))]
    (reduce #(removeRow %1 %2) board complete)))

(defn pushPieceDown [board]
  (if (canMove? (:currentPiece board) board down)
    (update board :currentPiece #(move % down board))
    (removeCompleteRowsAndScore (placePiece board))))

(defn canMovePieceDown? [board] (canMove? (:currentPiece board) board down))

(defn pushPieceDownFully [board]
  (if (canMovePieceDown? board)
    (recur (pushPieceDown board))
    board))

(defn isGameUnfinished? [board]
  (canFit? (randomPiece board) board))

(defn play [board-ref blockSize]
  (let [paintBlocks (fn [blocks g]
                      (doall
                        (map #(let [x (:x %)
                                    y (:y %)]
                                (.setColor g (:color %))
                                (.fillRect g (* x blockSize) (* y blockSize) blockSize blockSize)
                                (.setColor g Color/BLACK)
                                (.drawRect g (* x blockSize) (* y blockSize) blockSize blockSize)) blocks)))
        panel (proxy [JPanel] []
                (paintComponent [^Graphics g]
                  (proxy-super paintComponent g)
                  (.setColor g Color/WHITE)
                  (.fillRect g 0 0 (.getWidth this) (.getHeight this))
                  (paintBlocks (:currentPiece (deref board-ref)) g)
                  (paintBlocks (:blocks (deref board-ref)) g)
                  (.setColor g Color/BLACK)
                  (.setFont g (Font/decode "Arial-BOLD-18"))
                  (.drawString g (str "Score: " (:score (deref board-ref))) ^Integer (- (/ (.getWidth this) 2) 40) 20))
                )
        main (JFrame. "Tetris")
        myKeyListener (proxy [KeyAdapter] []
                        (keyPressed [e]
                          (condp = (.getKeyCode e)
                            KeyEvent/VK_DOWN (swap! board-ref pushPieceDown)
                            KeyEvent/VK_UP (swap! board-ref rotateCurrentPiece)
                            KeyEvent/VK_LEFT (swap! board-ref (fn [b] (update b :currentPiece #(move % left b))))
                            KeyEvent/VK_RIGHT (swap! board-ref (fn [b] (update b :currentPiece #(move % right b))))
                            KeyEvent/VK_SPACE (swap! board-ref pushPieceDownFully)
                            nil)
                          (.repaint panel)))

        gameThread (proxy [Thread] []
                     (run []
                       (println "Game is starting")
                       (while (isGameUnfinished? (deref board-ref))
                         (swap! board-ref pushPieceDown)
                         (.repaint panel)
                         (Thread/sleep (max (- 1000 (:score (deref board-ref))) 200)))
                       (println (str "Game is done! You scored " (:score (deref board-ref)) " points!"))))
        ]
    (.setPreferredSize panel (Dimension. (+ 1 (* (:width (deref board-ref)) blockSize)) (+ 1 (* (:height (deref board-ref)) blockSize))))
    (.addKeyListener panel myKeyListener)
    (.addKeyListener main myKeyListener)
    (.setResizable main false)
    (.setDefaultCloseOperation main JFrame/EXIT_ON_CLOSE)
    (.setLocationRelativeTo main nil)
    (.add main ^Component panel)
    (.pack main)
    (.setVisible main true)
    (.start gameThread)))

(defn -main
  "Start a game"
  []
  (let [board (atom (newPiece (Board. [] 10 20 (randomPiece) 0)))]
    (play board 30))
  )




























