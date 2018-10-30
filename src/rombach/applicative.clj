(ns rombach.applicative
  (:require [rombach.functor :as functor]
            [rombach.product :refer [defproduct]]))

(defproduct applicative applicative applicative?
  [[functor ::functor/functor]
   [pure fn?]
   [apply fn?]])
