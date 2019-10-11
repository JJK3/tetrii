;;; This file contains the most basic components of tetris.
;;; -------------------------------------------------------

;; Tests whether 2 lists of objects are equal?
(define list-equals? (lambda (x y)
                      (if (or (null? x) (null? y)) #t
                          (and (send (car x) equals? (car y))
                               (list-equals? (cdr x) (cdr y))))))

;; Selects a random element in the given list
(define random-in-list 
  (lambda (lst) 
    (list-ref lst
          (random (length lst)))))

;; Main Class. A single tetris block
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



;; A group of blocks that can be rotated and moved
(define block-group%
  (class* object% () ()
    ;; block-list : the list of block% objects
    ;; center     : a block% (the upper/left corner is used for rotation)
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

;; Create a new block-group given a list of relative xy pairs to the center.
;; ex. '((0 0) (0 1) (0 2)) etc.
(define make-block-group (lambda (center-x center-y pair-list color) 
                           (make-object block-group% 
                             (map (lambda (pair) (make-object block% 
                                                   (+ (car pair) center-x)
                                                   (+ (cadr pair) center-y)
                                                   color))
                                  pair-list)
                             (make-object block% center-x center-y color)
                             color)))
                     
;; Creates a t-shaped block with the given xy as the center
;; looks like:
;;  ###
;;   #
(define make-t-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (-1 0) (0 1)) "Firebrick")))

;; Creates the 4 long block with the given xy as the center
;; looks like:
;;   #
;;   #
;;   #
;;   #
(define make-I-group (lambda (x y) (make-block-group x y '((0 0) (0 1) (0 2) (0 3)) "blue" )))

;; Creates the L block with the given xy as the center
;; looks like: ###
;;             #
(define make-l1-group (lambda (x y) (make-block-group x y '((0 0) (-1 1) (-1 0) (1 0)) "green")))

;; Creates the L block with the given xy as the center
;; looks like: ###
;;               #  
(define make-l2-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (-1 0) (1 1)) "brown")))

;; Creates the square block with the given xy as the center
;; looks like: ##
;;             ##
(define make-square-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (0 1) (1 1)) "DarkOliveGreen")))

;; Creates the block with the given xy as the center
;; looks like: ##
;;            ##
(define make-n1-group (lambda (x y) (make-block-group x y '((0 0) (1 0) (0 1) (-1 1)) "Teal" )))

;; Creates the L block with the given xy as the center
;; looks like: ##
;;              ##
(define make-n2-group (lambda (x y) (make-block-group x y '((0 0) (-1 0) (0 1) (1 1)) "DarkOrange")))

(define group-functions (list make-t-group make-I-group make-l1-group make-l2-group make-square-group make-n1-group make-n2-group))
 
;; Select a random piece
(define get-random-group (lambda (x y) ((random-in-list group-functions) x y)))
