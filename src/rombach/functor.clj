(ns rombach.functor
  (:require [rombach.product :refer [defproduct]]))

(defproduct functor functor functor?
  [[fmap fn?]])

