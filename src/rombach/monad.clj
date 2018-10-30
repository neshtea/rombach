(ns rombach.monad
  (:require [clojure.spec.alpha :as s]
            [rombach.applicative :as applicative]
            [rombach.product :refer [defproduct]]))

(defproduct monad make-monad monad?
  [[applicative ::applicative/applicative]
   [return fn?]
   [bind fn?]
   [seq (s/or :none nil? :function fn?)]
   [fail (s/or :none nil? :function fn?)]])

(defn monad
  ([applicative return bind]
   (monad applicative return bind {:seq  #(throw (Exception. "Not yet implemented."))
                                   :fail #(throw (Exception. "Not yet implemented."))}))
  ([applicative return bind more]
   (make-monad applicative return bind (:seq more) (:fail more))))
