(ns rombach.data.semi-group
  (:require [rombach.structure.product :refer [defproduct]]))

(defproduct semi-group semi-group semi-group?
  [[cat fn?]])

(defn _cat
  [semi-group a b]
  ((semi-group-cat semi-group) a b))

;;;; Instances
(def _list (semi-group (fn [xs ys] (concat xs ys))))

(def _vec (semi-group (fn [xs ys] (into [] (concat xs ys)))))

(def _set (semi-group (fn [xs ys] (clojure.set/union xs ys))))
