(ns rombach.functor
  (:require [active.clojure.record :refer [define-record-type]]
            [clojure.spec.alpha :as s]
            [active.clojure.condition :as c]))

(define-record-type Functor
  (functor fmap) functor?
  [^{:doc "A functor (a -> b) -> f a -> f b"}
   fmap functor-fmap])

;; TODO refine specs
(s/def ::fmap (s/fspec :args (s/cat :f fn? :fa any?)
                       :ret any?))


