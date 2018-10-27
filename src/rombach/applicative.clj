(ns rombach.applicative
  (:require [active.clojure.record :refer [define-record-type]]
            [rombach.functor :as functor]))

(define-record-type Applicative
  (applicative functor pure apply) applicative?
  [^{:doc "The underlying functor."}
   functor applicative-functor
   ^{:doc "Function a -> f a, where f is a functor."}
   pure applicative-pure
   ^{:doc "Function f (a -> b) -> f a -> f b, where f is a functor."}
   apply applicative-apply])
