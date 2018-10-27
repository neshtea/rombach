(ns rombach.monoid
  (:require [active.clojure.record :refer [define-record-type]]))

(define-record-type Monoid
  (monoid mempty mappend) monoid?
  [mempty monoid-mempty
   mappend monoid-mappend])
