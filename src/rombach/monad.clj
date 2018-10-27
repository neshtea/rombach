(ns rombach.monad
  (:require [active.clojure.record :refer [define-record-type]]))

(define-record-type Monad
  (make-monad applicative return bind seq fail) monad?
  [applicative monad-applicative
   return monad-return
   bind monad-bind
   seq monad-seq
   fail monad-fail])

(defn monad
  ([applicative return bind]
   (monad applicative return bind {:seq  #(throw (Exception. "Not yet implemented."))
                                   :fail #(throw (Exception. "Not yet implemented."))}))
  ([applicative return bind more]
   (make-monad applicative return bind (:seq more) (:fail more))))
