(ns rombach.data.either-test
  (:require [rombach.data.either :as either]
            [clojure.test :refer :all]
            [clojure.spec.test.alpha :as stest]))

(stest/instrument)

(deftest either-test
  (let [fn-call (fn [e-a-b]
                  (either/either inc #(str % "!") e-a-b))]
    (is (= 42 (fn-call (either/right 41))))
    (is (= "foo!" (fn-call (either/left "foo"))))))

(deftest lefts-test
  (let [xs (list (either/right 21) (either/left 42) (either/right 32))]
    (is (= (list 42) (either/lefts xs)))
    (is (= '() (either/lefts '())))))

(deftest right-test
  (let [xs (list (either/right 21) (either/left 42) (either/right 32))]
    (is (= (list 21 32) (either/rights xs)))
    (is (= '() (either/rights '())))))

(deftest partition-eithers
  (is (= [[42] [21 32]]
         (either/partition-eithers (list (either/right 21) (either/left 42) (either/right 32)))))
  (is (= [[] [21 32]]
         (either/partition-eithers (list (either/right 21) (either/right 32)))))
  (is (= [[42] []]
         (either/partition-eithers (list (either/left 42)))))
  (is (= [[] []] (either/partition-eithers '()))))
