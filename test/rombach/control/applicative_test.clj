(ns rombach.control.applicative-test
  (:require [rombach.control.applicative :as app]
            [rombach.data.maybe :as maybe]
            [clojure.test :refer :all]))

(deftest applicative-pure-test
  (testing "app/list _pure returns a singleton list"
    (is (= (list 1) (app/_pure app/_list 1)))
    (is (= (list 42) (app/_pure app/_list (+ 21 21)))))
  (testing "app/vec _pure returns a singleton vector"
    (is (= [1] (app/_pure app/_vec 1)))
    (is (= [42] (app/_pure app/_vec (+ 21 21)))))
  (testing "applying a vector of functions to a vector of values"
    (is (= (list 2 3 4 0 1 2) (app/_apply app/_list (list inc dec) (list 1 2 3))))
    (is (= [2 3 4 0 1 2] (app/_apply app/_vec [inc dec] [1 2 3]))))
  (testing "fails when supplied the wrong value type"
    (try (app/_apply app/_vec (list inc dec) (list 1 2 3))
         (catch Exception e
           (is (= "not a value of type vector" (.getMessage e)))))))


(deftest liftA-2-test
  (is (= (list (list 1 :a) (list 1 :b))
         (app/liftA-2 app/_list
                  (fn [x] (fn [xs] (cons x xs)))
                  (list 1)
                  (list [:a] [:b]))))
  (is (= '()
         (app/liftA-2 app/_list
                  (fn [x] (fn [xs] (cons x xs)))
                  '()
                  (list [:a] [:b]))))
  (is (= (list (list 1 :a) (list 2 :a))
         (app/liftA-2 app/_list
                  (fn [x] (fn [xs] (cons x xs)))
                  (list 1 2)
                  (list [:a]))))
  (is (= (maybe/just (list 3 4))
         (app/liftA-2 maybe/applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  (maybe/just 3)
                  (maybe/just [4]))))
  (is (= (maybe/just 42)
         (app/liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  (maybe/just 2)
                  (maybe/just 40))))
  (is (= maybe/nothing
         (app/liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  maybe/nothing
                  (maybe/just 40))))
  (is (= maybe/nothing
         (app/liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  (maybe/just 2)
                  maybe/nothing)))
  (testing "fails when using a value that is not a list"
    (try (app/liftA-2 app/_list
                  (fn [x] (fn [xs] (cons x xs)))
                  [1 2 3 4 5]
                  (list [:a :b]))
         (catch Exception e
           (is (= "not a value of type list" (.getMessage e)))))))

(deftest sequenceA-test
  (is (= (maybe/just (list 3 2 1))
         (app/sequenceA maybe/applicative
                        (list (maybe/just 3)
                              (maybe/just 2)
                              (maybe/just 1)))))
  (is (= maybe/nothing
         (app/sequenceA maybe/applicative
                        (list (maybe/just 3)
                              maybe/nothing
                              (maybe/just 42)))))
  (is (= (list (list 1 4) (list 1 5) (list 1 6)
               (list 2 4) (list 2 5) (list 2 6)
               (list 3 4) (list 3 5) (list 3 6))
         (app/sequenceA app/_list (list (list 1 2 3) (list 4 5 6)))))
  (is (= '()
         (app/sequenceA app/_list (list (list 1 2 3) (list 4 5 6) '()))))
  (is (= '()
         (app/sequenceA app/_list (list (list 1 2 3) '() (list 4 5 6))))))
