(ns rombach.control.applicative
  (:require [rombach.data.functor :as functor]
            [rombach.structure.product :refer [defproduct]]))

(defproduct applicative applicative applicative?
  [[functor ::functor/functor]
   [pure fn?]
   [apply fn?]])

(defn _pure
   "Takes an applicative and some value x and 'lifts' it into the applicative."
   [applicative x]
   (when-not (applicative? applicative)
     (throw (ex-info "not an applicative" {:arguments [applicative]})))
   ((applicative-pure applicative) x))

(defn _apply
  "Takes an applicative, an applicative `f` and an applicative `x` of that
  applicative type and applicative-applies `f` to `x`."
  [applicative f x]
  (when-not (applicative? applicative)
    (throw (ex-info  "not an applicative" {:arguments [applicative]})))
  ((applicative-apply applicative) f x))

;; Instances
(def _list
  (applicative
   functor/_list
   list
   (fn [fs xs]
     (for [f fs
           x xs]
       (f x)))))

(def _vec
  (applicative
   functor/_vec
   list
   (fn [fs xs]
     (into [] (for [f fs
                    x xs]
                (f x))))))

(def _function
  (applicative
   functor/_function
   (fn [x] (fn [_] x))
   (fn [f g]
     (fn [x]
       (f x (g x))))))

(defn liftA-2
  [applicative f a b]
  (_apply applicative
          (functor/_fmap (applicative-functor applicative) f a)
          b))

(defn sequenceA
  [applicative a]
  (if (empty? a)
    (_pure applicative '())
    (_apply applicative
            (functor/_fmap
             (applicative-functor applicative)
             (fn [x] #(cons x %))
             (first a))
            (sequenceA applicative (rest a)))))
