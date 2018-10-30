(ns rombach.data.semi-group
  (:require [rombach.data.product :refer [defproduct]]))

(defproduct semi-group semi-group semi-group?
  [[cat fn?]])

(defn _cat
  [semi-group a b]
  ((semi-group-cat semi-group) a b))

;;;; Instances
(def list
  (semi-group (fn [xs ys] (concat xs ys))))

(def vec
  (semi-group (fn [xs ys] (into [] (concat xs ys)))))
