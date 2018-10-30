(ns rombach.data.maybe-test
  (:require [rombach.data.maybe :as maybe]
            [clojure.test :refer :all]
            [rombach.data.functor :as functor]))

(deftest maybe-test
  (is (= 42 (maybe/maybe 42 inc maybe/nothing)))
  (is (= 42 (maybe/maybe 42 inc (maybe/just 41)))))

(deftest from-just-test
  (is (= 42 (maybe/from-just (maybe/just 42))))
  (is (thrown? clojure.lang.ExceptionInfo (maybe/from-just maybe/nothing))))

(deftest list-to-maybe-test
  (is (= maybe/nothing (maybe/list-to-maybe '())))
  (is (= (maybe/just 1) (maybe/list-to-maybe (list 1 2 3)))))

(deftest maybe-to-list-test
  (is (= '() (maybe/maybe-to-list maybe/nothing)))
  (is (= (list 1) (maybe/maybe-to-list (maybe/just 1)))))

(deftest list-to-maybe-to-list-test
  (is (= (list 1) (maybe/maybe-to-list (maybe/list-to-maybe (list 1 2 3))))))

(deftest cat-maybes-test
  (is (= (list (maybe/just 1) (maybe/just 2))
         (maybe/cat-maybes (list maybe/nothing (maybe/just 1) (maybe/just 2) maybe/nothing))))
  (is (= '() (maybe/cat-maybes '()))))

(deftest map-maybe
  (testing "mapping the identity filters nothings"
    (is (= (list (maybe/just 1) (maybe/just 2))
           (maybe/map-maybe
            identity
            (list maybe/nothing (maybe/just 1) (maybe/just 2) maybe/nothing)))))
  (is (= (list (maybe/just 0) (maybe/just 2) (maybe/just 3) (maybe/just 0))
         (maybe/map-maybe
          (fn [ma]
            (if (maybe/just? ma)
              (functor/_fmap maybe/functor inc ma)
              (maybe/just 0)))
          (list maybe/nothing (maybe/just 1) (maybe/just 2) maybe/nothing)))))
