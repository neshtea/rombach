(ns rombach.control.monad-test
  (:require [rombach.control.monad :as m]
            [rombach.data.maybe :as maybe]
            [clojure.test :refer :all]
            [rombach.control.monad :as monad]))

(deftest list-monad-bind-test
  (is (= (list 3 -3 4 -4 5 -5)
         (m/_bind m/_list (list 3 4 5)
                  (fn [x] (list x (- x))))))
  (is (= '()
         (m/_bind m/_list '()
                  (fn [x] (list x (- x)))))))

(deftest monadic-list-test
  (is (= (list 3 -3 4 -4 5 -5)
         (m/_bind m/_list (list 3 4 5)
                   (fn [x] (list x (- x))))
         (m/monadic m/_list
                    [x (list 3 4 5)]
                    (list x (- x))))))

(deftest monad-maybe-test
  (is (= (maybe/just 43)
         (m/_bind maybe/monad (m/_return maybe/monad 42)
                  (fn [x] (m/_return maybe/monad (inc x))))
         (m/monadic maybe/monad
                    [x (maybe/just 42)]
                    (m/_return maybe/monad (inc x)))))

  (is (= (maybe/just 42)
         (m/monadic maybe/monad
                    [x (maybe/just 20)
                     y (maybe/just 22)]
                    (m/_return maybe/monad (+ x y)))))

  (is (= maybe/nothing
         (m/monadic maybe/monad
                    [x (maybe/just 20)
                     y maybe/nothing]
                    (m/_return maybe/monad (+ x y)))))

  (is (= maybe/nothing
         (m/monadic maybe/monad
                    [x maybe/nothing
                     y (maybe/just 20)]
                    (m/_return maybe/monad (+ x y))))))

(deftest mixing-monads-test
  (is (= (maybe/just true)
         (m/monadic maybe/monad
                    [x (m/_return maybe/monad
                                  (m/monadic m/_list
                                             [x (list 1 2 3)]
                                             (list x (- x))))]
                    (let [c (count x)])
                    (m/_return maybe/monad (even? c))))))
