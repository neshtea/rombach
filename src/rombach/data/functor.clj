(ns rombach.data.functor
  (:require [active.clojure.record :refer [define-record-type]]
            [active.clojure.condition :as c]))

(define-record-type Functor
  (functor fmap) functor?
  [fmap functor-fmap])

(defn _fmap
  [functor a->b fa]
  (if (functor? functor)
    ((functor-fmap functor) a->b fa)
    (c/assertion-violation `_fmap "not a functor" functor)))

(def _list
  (functor
   (fn [f xs]
     (when-not (list? xs)
       (c/assertion-violation `_list "not a value of type list" xs))
     (map f xs))))

(def _vec
  (functor
   (fn [f xs]
     (when-not (vector? xs)
       (c/assertion-violation `_vec "not a value of type vector" xs))
     (mapv f xs))))

(def _set
  (functor
   (fn [f xs]
     (when-not (set? xs)
       (c/assertion-violation `_set "not a value of type set" xs))
     (set (map f xs)))))

(def _map
  (functor
   (fn [f kvs]
     (when-not (map? kvs)
       (c/assertion-violation `_map "not a value of type map" kvs))
     (into {} (map (fn [[k v]] [k (f v)]) kvs)))))

(def _function
  (functor (fn [f g]
             (fn [x]
               (f (g x))))))
