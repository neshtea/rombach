(ns rombach.monad-transformer
  (:require [active.clojure.record :refer [define-record-type]]))

(define-record-type MonadTransformer
  (monad-transformer monad ) monad-transformer?
  [monad monad-transformer-monad
   run monad-transformer-run])

