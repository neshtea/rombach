(ns rombach.data.functor
  (:require [rombach.structure.product :refer [defproduct]]))

(defproduct functor functor functor?
  [[fmap fn?]])

(defn _fmap
  [functor a->b fa]
  (if (functor? functor)
    ((functor-fmap functor) a->b fa)
    (throw (ex-info "not a functor" {:arguments [functor]}))))

(def list
  (functor
   (fn [f xs]
     (when-not (list? xs)
       (throw (ex-info "not a value of type list" {:arguments [xs]})))
     (map f xs))))

(def vec
  (functor
   (fn [f xs]
     (when-not (vector? xs)
       (throw (ex-info "not a value of type vector" {:arguments [xs]})))
     (mapv f xs))))

(def set
  (functor
   (fn [f xs]
     (when-not (set? xs)
       (throw (ex-info "not a value of type set" {:arguments [xs]})))
     (set (map f xs)))))

(def map
  (functor
   (fn [f kvs]
     (when-not (map? kvs)
       (throw (ex-info "not a value of type map" {:arguments [kvs]})))
     (into {} (map (fn [[k v]] [k (f v)]) kvs)))))

(def function
  (functor (fn [f g]
             (fn [x]
               (f (g x))))))
