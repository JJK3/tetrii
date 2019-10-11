(ns clojure-tetris.core-test
  (:require [clojure.test :refer :all]
            [clojure-tetris.core :refer :all])
  (:import (java.awt Color)))

(deftest a-test
  (testing "Test move down"
    (let [board (->Board [] 10 30 nil 0)
          original (->Block 0 0 Color/RED)
          expected (->Block 0 1 Color/RED)]
      (is (= expected (move original down board))))))

(deftest a-test2
  (testing "Test pushPieceDown"
    (let [board (->Board [] 10 30 [(->Block 0 0 Color/RED)] 0)
          expected  [(->Block 0 1 Color/RED)]]
      (is (= expected (:currentPiece (pushPieceDown board)))))))


(run-tests)