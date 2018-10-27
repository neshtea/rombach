(ns rombach.core-test
  (:require [clojure.test :refer :all]
            [rombach.core :refer :all]
            [rombach.maybe :as maybe]))

;;;; List functor
(deftest list-functor-test
  (testing "mapping over an empty list"
    (is (= '() (_fmap list-functor inc '()))))
  (testing "mapping over a non-empty list"
    (is (= (list 2 3 4) (_fmap list-functor inc (list 1 2 3)))))
  (testing "fails when mapping over a non-list value"
    (try (_fmap list-functor inc [1 2 3])
         (catch Exception e
           (is (= "not a value of type list" (.getMessage e)))))))

(deftest vec-functor-test
  (testing "mapping over an empty vector"
    (is (= [] (_fmap vec-functor inc []))))
  (testing "mapping over a non-empty list"
    (is (= [2 3 4] (_fmap vec-functor inc [1 2 3]))))
  (testing "fails when mapping over a non-vector value"
    (try (_fmap vec-functor inc (list 1 2 3))
         (catch Exception e
           (is (= "not a value of type vector" (.getMessage e)))))))

(deftest applicative-pure-test
  (testing "list-applicative _pure returns a singleton list"
    (is (= (list 1) (_pure list-applicative 1)))
    (is (= (list 42) (_pure list-applicative (+ 21 21)))))
  (testing "vec-applicative _pure returns a singleton vector"
    (is (= [1] (_pure vec-applicative 1)))
    (is (= [42] (_pure vec-applicative (+ 21 21)))))
  (testing "applying a vector of functions to a vector of values"
    (is (= (list 2 3 4 0 1 2) (_apply list-applicative (list inc dec) (list 1 2 3))))
    (is (= [2 3 4 0 1 2] (_apply vec-applicative [inc dec] [1 2 3]))))
  (testing "fails when supplied the wrong value type"
    (try (_apply vec-applicative (list inc dec) (list 1 2 3))
         (catch Exception e
           (is (= "not a value of type vector" (.getMessage e)))))))

(deftest liftA-2-test
  (is (= (list (list 1 :a) (list 1 :b))
         (liftA-2 list-applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  (list 1)
                  (list [:a] [:b]))))
  (is (= '()
         (liftA-2 list-applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  '()
                  (list [:a] [:b]))))
  (is (= (list (list 1 :a) (list 2 :a))
         (liftA-2 list-applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  (list 1 2)
                  (list [:a]))))
  (is (= (maybe/just (list 3 4))
         (liftA-2 maybe/applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  (maybe/just 3)
                  (maybe/just [4]))))
  (is (= (maybe/just 42)
         (liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  (maybe/just 2)
                  (maybe/just 40))))
  (is (= maybe/nothing
         (liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  maybe/nothing
                  (maybe/just 40))))
  (is (= maybe/nothing
         (liftA-2 maybe/applicative
                  (fn [a] (fn [b] (+ a b)))
                  (maybe/just 2)
                  maybe/nothing)))
  (testing "fails when using a value that is not a list"
    (try (liftA-2 list-applicative
                  (fn [x] (fn [xs] (cons x xs)))
                  [1 2 3 4 5]
                  (list [:a :b]))
         (catch Exception e
           (is (= "not a value of type list" (.getMessage e)))))))

(deftest sequenceA-test
  (is (= (maybe/just (list 3 2 1))
         (sequenceA maybe/applicative
                    (list (maybe/just 3)
                          (maybe/just 2)
                          (maybe/just 1)))))
  (is (= maybe/nothing
         (sequenceA maybe/applicative
                    (list (maybe/just 3)
                          maybe/nothing
                          (maybe/just 42)))))
  (is (= (list (list 1 4) (list 1 5) (list 1 6)
               (list 2 4) (list 2 5) (list 2 6)
               (list 3 4) (list 3 5) (list 3 6))
         (sequenceA list-applicative (list (list 1 2 3) (list 4 5 6)))))
  (is (= '()
         (sequenceA list-applicative (list (list 1 2 3) (list 4 5 6) '()))))
  (is (= '()
         (sequenceA list-applicative (list (list 1 2 3) '() (list 4 5 6))))))
