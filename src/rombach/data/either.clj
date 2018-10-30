(ns rombach.data.either
  (:require [active.clojure.condition :as c]
            [clojure.spec.alpha :as s]
            [rombach.control.applicative :as applicative]
            [rombach.data.functor :as functor]
            [rombach.structure.product :refer [defproduct]]))

(defproduct left left left?
  [[a any?]])

(defproduct right right right?
  [[b any?]])

(defn either?
  [obj]
  (or (left? obj) (right? obj)))

(s/def ::either (s/or :left ::left
                      :right ::right))

;;;; Utility functions.
(defn- fail-either
  [sym e-a]
  (c/assertion-violation sym "not a value of type either" e-a))

(defn either
  "Case analysis for the Either type. If the value is Left a, apply the first
  function to a; if it is Right b, apply the second function to b."
  [a->c b->c e-a-b]
  (cond
    (left? e-a-b) (b->c (left-a e-a-b))
    (right? e-a-b) (a->c (right-b e-a-b))
    :else (fail-either `either e-a-b)))

(defn lefts
  "Extracts from a list of Either all the Left elements.
  All the Left elements are extracted in order."
  [e-a-bs]
  (->> e-a-bs
       (filter left?)
       (map (fn [e-b]
              (when-not (left? e-b) (fail-either `lefts e-b))
              (left-a e-b)))))

(defn rights
  "Extracts from a list of Either all the Right elements.
  All the Right elements are extracted in order."
  [e-a-bs]
  (->> e-a-bs
       (filter right?)
       (map (fn [e-a]
              (when-not (right? e-a) (fail-either `rights e-a))
              (right-b e-a)))))

(defn partition-eithers
  "Partitions a list of Either into two lists.
  All the Left elements are extracted, in order, to the first component of the output.
  Similarly the Right elements are extracted to the second component of the output."
  [e-a-bs]
  (reduce (fn [[lefts rights] e-a-b]
            (cond
              (left? e-a-b) [(conj lefts (left-a e-a-b)) rights]
              (right? e-a-b) [lefts (conj rights (right-b e-a-b))]
              :else (fail-either `partition-eithers e-a-b)))
          [[] []] e-a-bs))

;;;; Typeclass implementations
(def functor
  (functor/functor
   (fn [f e]
     (cond
       (left? e)  e
       (right? e) (right (f (right-b e)))
       :else (c/assertion-violation `either-applicative "not a value of type either" e)))))

(defn fmap
  [f e]
  ((functor/functor-fmap functor) f e))

(def applicative
  (applicative/applicative
   functor
   right
   (fn [ea eb]
     (cond
       (left? ea) ea
       (right? ea)
       ((functor/functor-fmap functor) (right-b ea) eb)
       :else (c/assertion-violation `either-applicative "not a value of type either" ea eb)))))

(defn pure
  [x]
  ((applicative/applicative-pure applicative) x))

(defn apply
  [f e]
  ((applicative/applicative-apply applicative) f e))
