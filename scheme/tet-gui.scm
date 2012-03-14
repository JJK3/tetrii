(load "blocks.scm")
(define red (make-object color% "red"))
(define blue (make-object color% "blue"))
(define BLOCK_SIZE 25) ;;the graphical size of each block

(define test-grp2 (make-l1-group 5 1))
(define block-list (get-field block-list test-grp2))

;;the board painter
(define painter (lambda (canvas dc)
                  (map 
                   (lambda (block)   
                     (send dc set-brush (make-object brush% red 'solid))
                     (send dc draw-rectangle 
                           (* (get-field x block) BLOCK_SIZE) 
                           (* (- (get-field y block) 1) BLOCK_SIZE) BLOCK_SIZE BLOCK_SIZE)) 
                   block-list)))

;; send-and-set : generic -> void
;;a helper function to send a message and set the current block
(define send-and-set (lambda (generic)
                       (set! test-grp2 (send-generic test-grp2 generic))
                       (set! block-list (get-field block-list test-grp2))
                       (refresh-canvas)))

;;my own version of frame% that contains key-listeners
(define my-frame%
  (class frame% () ()
    (override on-subwindow-char)
    (define on-subwindow-char 
      (lambda (receiver char) 
        (let ((key-code (send char get-key-code)))
          (cond ((eq? key-code 'up)
                 (send-and-set (generic block-group% rot-left)))
                ((eq? key-code 'down)
                 (send-and-set (generic block-group% move-down)))
                ((eq? key-code 'left)
                 (send-and-set (generic block-group% move-left)))
                ((eq? key-code 'right)
                 (send-and-set (generic block-group% move-right)))
                ((eq? key-code #\space)
                 (send-and-set (generic block-group% rot-left)));TODO: make this sent the block all the way to the bottom
                ))))
  (super-new)))

;;main window
(define main-frame (instantiate my-frame% ("Tetris" #f 400 600)))
(define main-canvas (make-object canvas% main-frame null painter))
(send main-frame show #t)

;;refresh-canvas : void -> void 
;;helper function to clear and refresh the canvas
(define refresh-canvas (lambda () 
                         (send (send main-canvas get-dc) clear) ;refresh the canvas
                         (send main-canvas refresh)))

;;the timer object that shifts the current piece down every so often
(define down-timer (make-object timer% 
                     (lambda ()
                       (send-and-set (generic block-group% move-down)))
                     1000 #f))
