

(define player<%>
  (interface ()
    refresh ;; block% -> void
    take-piece
    message ;; string -> void
    ))

(define main-block-list '())
(define main-frame 5)
(define main-canvas 6)

(define human-player%
  (class* object% (player<%>) ()
    (init-field board)
    (field (score 0))
    (public* 
     (init-gui (lambda () 
                 ;;main window
                 (set! main-frame (instantiate my-frame% (board "Tetris" #f 
                                                                (* (- (get-field width board) 1) BLOCK_SIZE)
                                                                (* (+ (get-field height board) 1) BLOCK_SIZE)
                                                                )))
                 (send main-frame init-gui)
                 (set! main-canvas (make-object canvas% main-frame null painter))
                 (send main-frame show #t)
                 
                 ))
     (refresh (lambda () (refresh-canvas)))
     (take-piece (lambda (piece) 5))
     (message (lambda (msg) (display msg)))
     (inc-score (lambda (num) (set! score (+ score num)) 
                  (send (get-field score-box main-frame) 
                        set-label (string-append "Score: " (number->string score))))))
    (super-new)))

(define BLOCK_SIZE 30) ;;the graphical size of each block

;;the board painter
(define painter (lambda (canvas dc)
                  (map 
                   (lambda (block)   
                     (send dc set-brush (make-object brush% (get-field color block) 'solid))
                     (send dc draw-rectangle 
                           (* (- (get-field x block) 1) BLOCK_SIZE) 
                           (* (- (get-field y block) 1) BLOCK_SIZE) 
                           BLOCK_SIZE 
                           BLOCK_SIZE)) 
                   main-block-list)))

;;my own version of frame% that contains key-listeners
(define my-frame%
  (class frame% () ()
    (field (score-box null))
    (init-field board)
    (override on-subwindow-char)
    (define on-subwindow-char 
      (lambda (receiver char) 
        (let ((key-code (send char get-key-code)))
          (cond ((eq? key-code 'up)
                 (send board piece-rot!) (refresh-canvas board))
                ((eq? key-code 'down)
                 (send board piece-down!) (refresh-canvas board))
                ((eq? key-code 'left)
                 (send board piece-left!) (refresh-canvas board))
                ((eq? key-code 'right)
                 (send board piece-right!) (refresh-canvas board))
                ((eq? key-code #\space)
                 (send board piece-full-down!) (refresh-canvas board))
                ((eq? key-code #\backspace)
                 (send board set-piece! (make-I-group (/ (get-field width board) 2) 1)))
                ))))
    (public*
     (init-gui (lambda () (set! score-box (make-object message% "Score:" this))
                 (send score-box stretchable-width #t))))
    (super-new)))

;;refresh-canvas : void -> void 
;;helper function to clear and refresh the canvas
(define refresh-canvas (lambda (board) 
                         (set! main-block-list (append (get-field block-list (get-field piece board) )
                                                       (get-field block-list board)))
                         (send (send main-canvas get-dc) clear) ;refresh the canvas
                         (send main-canvas refresh)))

(define ai-player%
  (class* object% (player<%>) ()
    (init-field board)
    (public* 
     (refresh (lambda () 5))
     (message (lambda (msg) (display msg)))
     (take-piece (lambda (piece) 5))
     )
    (super-new)))