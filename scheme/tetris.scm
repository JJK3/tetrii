(load "blocks.scm")
(load "board.scm")
(load "player.scm")

(define server% 
  (class* object% () ()
    (init-field board player)
    (field (down-timer null))
    (public* 
     (start-game (lambda () 
                   (let ((piece (get-random-group (/ (get-field width board) 2) 1)))
                     (send board set-piece! piece)
                     (send the-player take-piece piece))
                   (set! down-timer  
                         (make-object timer% 
                           (lambda ()
                             (send board piece-down!)
                             (refresh-canvas board))
                           500 #f))
                   (send the-player message "game is starting\n")))
     (stop (lambda () 
             (send down-timer stop) 
             (display "Game over") )))
    (super-new)))
#|
;;tests
(define test-board (make-object board% 30 20))
(assert-true "group fit 1" (send test-board will-group-fit? (make-I-group 5 1)))
(send test-board set-piece! (make-I-group 5 1))
(send test-board add-piece!)
(assert-true "group fit 2"  (send test-board will-group-fit? (make-I-group 6 1)))
(assert-true "group fit 3" (not (send test-board will-group-fit? (make-I-group 5 3))))

(assert-true "touch bottom 1" (not (send test-board is-group-touching-bottom?
                                         (make-I-group 5 1))))
(assert-true "touch bottom 2" (send test-board is-group-touching-bottom?
                                    (make-I-group 5 26)))
|#


(define board (make-object board% 20 12))
(define the-player (make-object human-player% board))
(send the-player init-gui)
(define server (make-object server% board the-player))
(send server start-game)
  




