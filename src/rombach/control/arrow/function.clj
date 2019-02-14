(ns rombach.control.arrow.function
  (:refer-clojure :exclude [first second])
  (:require [rombach.control.arrow :as a]
            [rombach.control.category :as c]))

(def function-arrow
  (a/arrow c/function-category
           identity
           ;; a b c -> a (b, d) (c, d)
           (fn [f]
             (fn [[x y]]  ; b c
               [(f x) y]))))

(defn arr [f] (a/arr function-arrow f))

(defn >>> [f g] (a/>>> function-arrow f g))

(defn >>>* [f g & hs] (apply a/>>>* function-arrow f g hs))

(defn <<< [f g] (a/<<< function-arrow f g))

(defn first [f] (a/first function-arrow f))

(defn second [f] (a/second function-arrow f))

(defn split [f g] (a/split function-arrow f g))

(defn fanout [f g] (a/fanout function-arrow f g))

(defn unsplit [op] (arr (fn [[x y]] (op x y))))

(defn liftA2
  [fun f g]
  (>>> (fanout f g) (unsplit fun)))
