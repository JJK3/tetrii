(load "blocks.scm")

;; a little testing framework
(define (assert-true desc bool) 
  (if (not bool) 
      (begin (printf "Test failed:\n  ") (printf desc))))


;;;Block Tests
(define test-block1 (make-object block% 5 7 "red"))
(define test-block2 (send test-block1 down))
(define test-block3 (send test-block2 left))
(define test-block4 (send test-block3 up))
(define test-block5 (send test-block4 right))
(assert-true "test x move in all directions" (= (get-field x test-block5) 5))
(assert-true "test y move in all directions" (= (get-field y test-block5) 7))
(assert-true "test equals" (send test-block1 equals? test-block5))  



;;Block Group Tests
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