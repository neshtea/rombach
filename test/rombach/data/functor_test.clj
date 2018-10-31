(ns rombach.data.functor-test
  (:require [rombach.data.functor :as functor]
            [clojure.test :refer :all]))

;;;; List functor
(deftest list-functor-test
  (testing "mapping over an empty list"
    (is (= '() (functor/_fmap functor/_list inc '()))))
  (testing "mapping over a non-empty list"
    (is (= (list 2 3 4) (functor/_fmap functor/_list inc (list 1 2 3)))))
  (testing "fails when mapping over a non-list value"
    (try (functor/_fmap functor/_list inc [1 2 3])
         (catch Exception e
           (is (= "not a value of type list" (.getMessage e)))))))

(deftest vec-functor-test
  (testing "mapping over an empty vector"
    (is (= [] (functor/_fmap functor/_vec inc []))))
  (testing "mapping over a non-empty list"
    (is (= [2 3 4] (functor/_fmap functor/_vec inc [1 2 3]))))
  (testing "fails when mapping over a non-vector value"
    (try (functor/_fmap functor/_vec inc (list 1 2 3))
         (catch Exception e
           (is (= "not a value of type vector" (.getMessage e)))))))
