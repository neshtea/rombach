(ns rombach.data.monoid
  (:require [rombach.data.product :refer [defproduct]]
            [rombach.data.semi-group :as semi-group]))

(defproduct monoid monoid monoid?
  [[semi-group ::semi-group/semi-group]
   [mempty any?]])

(defn _mempty
  "Takes a monoid and returns the zero element of that monoid."
  [monoid]
  (when-not (monoid? monoid)
    (throw (ex-info "not a monoid" {:arguments [monoid]})))
  (monoid-mempty monoid))

(defn _mappend
  [monoid a b]
  ((semi-group/_cat (monoid-semi-group monoid) a b)))

;;;; Instances
(def list-monoid
  (monoid semi-group/list '()))

(def vec-monoid
  (monoid semi-group/vec []))

