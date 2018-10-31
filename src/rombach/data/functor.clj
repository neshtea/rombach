(ns rombach.data.functor
  (:require [rombach.structure.product :refer [defproduct]]))

(defproduct functor functor functor?
  [[fmap fn?]])

(defn _fmap
  [functor a->b fa]
  (if (functor? functor)
    ((functor-fmap functor) a->b fa)
    (throw (ex-info "not a functor" {:arguments [functor]}))))

(def _list
  (functor
   (fn [f xs]
     (when-not (list? xs)
       (throw (ex-info "not a value of type list" {:arguments [xs]})))
     (map f xs))))

(def _vec
  (functor
   (fn [f xs]
     (when-not (vector? xs)
       (throw (ex-info "not a value of type vector" {:arguments [xs]})))
     (mapv f xs))))

(def _set
  (functor
   (fn [f xs]
     (when-not (set? xs)
       (throw (ex-info "not a value of type set" {:arguments [xs]})))
     (set (map f xs)))))

(def _map
  (functor
   (fn [f kvs]
     (when-not (map? kvs)
       (throw (ex-info "not a value of type map" {:arguments [kvs]})))
     (into {} (map (fn [[k v]] [k (f v)]) kvs)))))

(def _function
  (functor (fn [f g]
             (fn [x]
               (f (g x))))))
