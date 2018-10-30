(ns rombach.monoid
  (:require [rombach.product :refer [defproduct]]))

(defproduct monoid monoid monoid?
  [[mempty any?]
   [mappend fn?]])
