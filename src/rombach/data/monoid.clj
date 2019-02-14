(ns rombach.data.monoid
  (:require [active.clojure.record :refer [define-record-type]]
            [rombach.data.semi-group :as semi-group]))

(define-record-type Monoid
  (monoid semi-group mempty) monoid?
  [semi-group monoid-semi-group
   mempty monoid-mempty])

(defn _mempty
  "Takes a monoid and returns the zero element of that monoid."
  [monoid]
  (when-not (monoid? monoid)
    (throw (ex-info "not a monoid" {:arguments [monoid]})))
  (monoid-mempty monoid))

(defn _mappend
  [monoid a b]
  (semi-group/_cat (monoid-semi-group monoid) a b))

;;;; Instances
(def _list (monoid semi-group/_list '()))

(def _vec (monoid semi-group/_vec []))

