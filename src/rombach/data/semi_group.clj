(ns rombach.data.semi-group
  (:require [active.clojure.record :refer [define-record-type]]))

(define-record-type SemiGroup
  (semi-group cat) semi-group?
  [cat semi-group-cat])

(defn _cat
  [semi-group a b]
  ((semi-group-cat semi-group) a b))

;;;; Instances
(def _list (semi-group (fn [xs ys] (concat xs ys))))

(def _vec (semi-group (fn [xs ys] (into [] (concat xs ys)))))

(def _set (semi-group (fn [xs ys] (clojure.set/union xs ys))))
