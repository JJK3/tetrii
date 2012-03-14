;; a little testing framework
(define (assert-true desc bool) 
  (if (not bool) 
      (begin (printf "test failed:\n  ") (printf desc))))

;; given a list of booleans, are they all true?
(define and-list (lambda (lst) 
                   (if (null? lst) 
                       #t
                       (and (car lst) 
                            (and-list (cdr lst))))))

;;tests whether 2 lists of objects are equal?
(define list-equals? (lambda (x y)
                      (if (or (null? x) (null? y)) #t
                          (and (send (car x) equals? (car y))
                               (list-equals? (cdr x) (cdr y))))))

;; a single tetris block
(define block%
  (class* object% () ()
    (init-field x y color)
    (public*
     ;; makes new blocs but moved
     (up (lambda () (make-object block% x (- y 1) color)))
     (down (lambda () (make-object block% x (+ y 1) color)))
     (left (lambda () (make-object block% (- x 1) y color)))
     (right (lambda () (make-object block% (+ x 1) y color)))
     (equals? (lambda (block2) (and (= (get-field x block2) x)
                                   (= (get-field y block2) y)))))
    (super-new))) 

;;;tests
(define test-block1 (make-object block% 5 7 "red"))
(define test-block2 (send test-block1 down))
(define test-block3 (send test-block2 left))
(define test-block4 (send test-block3 up))
(define test-block5 (send test-block4 right))
(assert-true "test x move in all directions" (= (get-field x test-block5) 5))
(assert-true "test y move in all directions" (= (get-field y test-block5) 7))
(assert-true "test equals" (send test-block1 equals? test-block5))  

;; a group of blocks that can be rotated and moved
(define block-group%
  (class* object% () ()
    ;; block-list is the list of block% objects
    ;; center is a block% (the upper/left corner is used for rotation)
    (init-field block-list center (color null))
    (private* 
     ;;get's a blocks relative position to the center
     (relative-to-center (lambda (block) (make-object block% 
                                           (- (get-field x center)
                                              (get-field x block))
                                           (- (get-field y block)
                                              (get-field y center))
                                           color)))
     ;;returns a relative position to the real one
     (from-relative (lambda (block) (make-object block% 
                                      (- (get-field x center)
                                         (get-field x block))
                                      (+ (get-field y block)
                                         (get-field y center))
                                      color)))
     (move-helper (lambda (action) (make-object block-group% (map action block-list)
                                     (action center)
                                     color))))
    (public*
     ;; makes a new rotated block-group
     (rot-left (lambda () 
                 (make-object block-group%
                   (map (lambda (x) (from-relative x)) ;return from relative positions
                        (map (lambda (blck) (make-object block%  ;apply the rotation
                                              (get-field y blck) 
                                              (- (get-field x blck))
                                              color))
                             (map (lambda (x) (relative-to-center x)) ;transform into relative positions
                                  block-list)))
                   center 
                   color)))
     ;; makes a new block-group shifted down
     (down (lambda () (move-helper (lambda (x) (send x down))))) 
     ;; makes a new block-group shifted right
     (right (lambda ()(move-helper (lambda (x) (send x right))))) 
     ;; makes a new block-group shifted left
     (left (lambda ()(move-helper (lambda (x) (send x left))))) 
     ;; 2 groups are equal if their block-lists are equal
     ;; makes a new block-group shifted left
     (equal? (lambda (group) (list-equals? block-list (get-field block-list group)))))
    (super-new)))

;;create a new block-group given a list of relative xy pairs to the center.
;;ex. '((0 0) (0 1) (0 2)) etc.
(define make-block-group (lambda (center-x center-y pair-list color) 
                           (make-object block-group% 
                             (map (lambda (pair) (make-object block% 
                                                   (+ (car pair) center-x)
                                                   (+ (cadr pair) center-y)
                                                   color))
                                  pair-list)
                             (make-object block% center-x center-y color)
                             color)))
                     
;;creates a t-shaped block with the given xy as the center
;; looks like:
;;  ###
;;   #
(define make-t-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (-1 0) (0 1)) "Firebrick")))

;;creates the 4 long block with the given xy as the center
;; looks like:
;;   #
;;   #
;;   #
;;   #
(define make-I-group (lambda (x y) (make-block-group x y '((0 0) (0 1) (0 2) (0 3)) "blue" )))

;;creates the L block with the given xy as the center
;; looks like: ###
;;             #
(define make-l1-group (lambda (x y) (make-block-group x y '((0 0) (-1 1) (-1 0) (1 0)) "green")))

;;creates the L block with the given xy as the center
;; looks like: ###
;;               #  
(define make-l2-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (-1 0) (1 1)) "brown")))

;;creates the square block with the given xy as the center
;; looks like: ##
;;             ##
(define make-square-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (0 1) (1 1)) "DarkOliveGreen")))

;;creates the block with the given xy as the center
;; looks like: ##
;;            ##
(define make-n1-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (0 1) (-1 1)) "Teal" )))

;;creates the L block with the given xy as the center
;; looks like: ##
;;              ##
(define make-n2-group (lambda (x y) (make-block-group x y '((0 0) (-1 0) (0 1) (1 1)) "DarkOrange")))

;;tests
(define test-grp1 (make-object block-group% 
                    (list test-block1 test-block4) test-block1 "red"))
(let ((new-list
       (get-field block-list
                  (send test-grp1 down))))
  (assert-true "test group down1"  (send (car new-list) equals? (make-object block% 5 8 "red")))
  (assert-true "test group down2"  (send (cadr new-list) equals? (make-object block% 4 8 "red"))))
        
(assert-true "full rotation test1"  (send (send (send (send (send 
                                                             (make-t-group 2 2) rot-left) 
                                                            rot-left)
                                                      rot-left)
                                                rot-left)
                                          equal? (make-t-group 2 2)))

;;picks the x-th element in a list (0 index)
(define pick (lambda (lst x) 
               (if (= 0 x) (car lst)
                   (pick (cdr lst) (- x 1)))))

;;selects a random element in the given list
(define random-in-list 
  (lambda (lst) 
    (pick lst
          (random (length lst)))))
  
;;select a random piece
(define get-random-group 
  (lambda (x y) (random-in-list
              (list (make-t-group x y)
                    (make-I-group x y)
                    (make-l1-group x y)
                    (make-l2-group x y)
                    (make-square-group x y)
                    (make-n1-group x y)
                    (make-n2-group x y)))))
