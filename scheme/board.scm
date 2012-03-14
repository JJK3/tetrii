(define board%
  (class* object% () ()
    (init-field height width)
    (field (block-list null)
           (piece null)) ;;the current piece
    (private* 
     ;;shift all blocks down that are above a given y value
     (shift-down! (lambda (y)  
                    (set! block-list
                          (map (lambda (blk) (if (< (get-field y blk) y) 
                                                 (send blk down) 
                                                 blk))
                               block-list))))
     (move-helper (lambda (action) (if (will-group-fit? (send-generic piece action))
                                       (begin
                                         (set! piece (send-generic piece action))
                                         #t)
                                       #f))))
    (public*
     ;;remove a row of blocks
     (remove-row (lambda (y)       
                   (let ((temp-list (list)))
                     (map (lambda (block)  
                            (if (not (= (get-field y block) y))
                                (set! temp-list (cons block temp-list))))
                          block-list)
                     (set! block-list temp-list))))
     
     ;;check the given row to see if it's complete. returns #f if not
     (check-row 
      (lambda (y)
        (letrec ((loop-x 
                  (lambda (x)
                    (if (>= x 1)
                        (if (is-space-empty? x y)
                            #f
                            (begin (loop-x (- x 1))))
                        (begin 
                          ;;got here so the whole row exists
                          (remove-row y)
                          (shift-down! y))))))
          (loop-x (- width 1)))))

     (will-block-fit? (lambda (block) 
                        (let ((x (get-field x block))
                              (y (get-field y block)))
                          (and (>= x 1) (>= y 1) 
                               (< y height) (< x width)
                               (and-list (map
                                          (lambda (blk) (not  (and (= x (get-field x blk))
                                                                   (= y (get-field y blk)))))
                                          block-list))))))
     (will-group-fit? (lambda (group) 
                        (and-list    
                         (map (lambda (x) (will-block-fit? x))
                              (get-field block-list group)))))
     
     ;;is the given xy coordinate empty? : (x y) -> boolean
     (is-space-empty? (lambda (test-x test-y) 
                        (and-list (map 
                                   (lambda (block) (not (and 
                                                         (= test-x (get-field x block))
                                                         (= test-y (get-field y block)))))
                                   block-list))))
     
     ;;is the given block group touching the bottom blocks?
     ;;ASSUME: the given group has not yet been added
     (is-group-touching-bottom? (lambda (group) 
                                  (and (will-group-fit? group)
                                       (not (will-group-fit? 
                                             (send group down))))))
     
     ;;add the current piece to the board, reassigns the piece to a new random piece
     ;;ASSUME: group will fit
     (add-piece! (lambda () 
                   (set! block-list 
                         (append (get-field block-list piece) block-list))
                   (let ((p (get-random-group (/ width 2) 1)))
                     (set! piece p)
                     (send the-player take-piece p))
                   (if (or (is-group-touching-bottom? piece)
                           (not (will-group-fit? piece)))
                       (send server stop))
                   (update)))
     
     ;;creates a new instance of the board with the given piece
     (set-piece! (lambda (p) (set! piece p)))
     (piece-down! (lambda () 
                    (if (is-group-touching-bottom? piece)
                        (add-piece!))
                    (move-helper (generic block-group% down))))
     (piece-full-down! (lambda () (if (not (is-group-touching-bottom? piece))
                                      (begin
                                        (if (piece-down!)
                                            (piece-full-down!))))))
                                           
     (piece-left! (lambda () (move-helper (generic block-group% left))))
     (piece-rot! (lambda () (move-helper (generic block-group% rot-left))))
     (piece-right! (lambda () (move-helper (generic block-group% right))))
     ;;updates all rows below the given row number.  returns the number of completed rows.
     (update-helper (lambda (y) 
                      (if (>= y 1) 
                          (+ (update-helper (- y 1))
                             (if (check-row y)
                                 1
                                 0))
                          0)))
     ;;checks whether there are any complete rows.  
     ;;if there are, remove them, shift down and return true, and update the player's score
     ;;else return false
     (update (lambda () (let [(num (update-helper height))]
                          (if (> num 0) 
                              (let ((points (cond [(= num 1) 10]
                                                  [(= num 2) 22]
                                                  [(= num 3) 35]
                                                  [(= num 4) 50])))
                                (send the-player inc-score points)))))))
    (super-new)))