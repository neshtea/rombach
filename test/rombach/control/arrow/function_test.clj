(ns rombach.control.arrow.function-test
  (:require [rombach.control.arrow.function :as arrow]
            [clojure.test :refer [deftest is]]))

(deftest simple-test
  (let [f (arrow/arr #(/ % 2))
        g (arrow/arr (fn [x] (+ (* x 3) 1)))
        h (arrow/liftA2 + f g)]
    (is (= 29 (h 8)))))
