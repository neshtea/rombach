(ns rombach.structure.sum-test
  (:require [clojure.test :refer :all]
            [rombach.structure.product :refer [defproduct]]
            [rombach.structure.sum :refer [defsum]]))

(defproduct some make-some some? [[a any?]])
(defproduct none make-none none? [])

(defsum optional optional? [[some? ::some]
                            [none? ::none]])

(deftest predicate-test
  (is (optional? (make-some 42)))
  (is (optional? (make-none)))
  (is (not (optional? 42))))
