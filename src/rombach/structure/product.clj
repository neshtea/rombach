(ns rombach.structure.product
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [rombach.control.lens :as lens]))

(defmacro defproduct
  [?struct-name ?constructor-name ?predicate-name ?fields]
  `(do
     ;; Specs
     ;; The record field values.
     ~@(for [field ?fields]
         (when (vector? field)
           (let [[field-name field-spec] field]
             `(s/def ~(keyword (str *ns*) (str ?struct-name "-" field-name)) ~field-spec))))

     ;; The record.
     (s/def ~(keyword (str *ns*) (str ?struct-name))
       (s/spec (s/and (s/keys :req [~@(map (fn [[field-name _]]
                                             (keyword (str *ns*) (str ?struct-name "-" field-name)))
                                           ?fields)])
                      #(= (quote ~?struct-name) (get % :_struct)))
               :gen (fn []
                      (sgen/fmap (fn [m#] (assoc m# :_struct (quote ~?struct-name)))
                                 (s/gen (s/keys :req [~@(map (fn [[field-name _]]
                                                               (keyword (str *ns*) (str ?struct-name "-" field-name)))
                                                             ?fields)]))))))

     ;; Functions
     ;; Constructor.
     (defn ~?constructor-name
       [~@(map first ?fields)]
       (merge {:_struct (quote ~?struct-name)}
              ~(into {} (map (fn [k]
                               [(keyword (str *ns*) (str ?struct-name "-" (first k))) (first k)]) ?fields))))

     (s/fdef ~?constructor-name
       :args (s/cat ~@(apply concat (map (fn [[k v]] [(keyword k) v]) ?fields)))
       :ret ~(keyword (str *ns*) (str ?struct-name)))

     ;; Fields
     ~@(for [field ?fields]
         (let [[field-name _] field]
           `(do
              (defn ~(symbol (str ?struct-name "-" field-name))
                [~(quote ?struct-name)]
                (when-not (s/valid? ~(keyword (str *ns*) (str ?struct-name)) ~(quote ?struct-name))
                  (throw
                   (ex-info "Supplied value does not match specification."
                            {:argument [~(quote ?struct-name)]
                             :explanation
                             (s/explain-str ~(keyword (str *ns*) (str ?struct-name)) ~(quote ?struct-name))})))
                (get ~(quote ?struct-name) ~(keyword (str *ns*) (str ?struct-name "-" field-name))))
              (def ~(symbol (str ?struct-name "-" field-name "-lens"))
                (lens/lens (fn [obj#]
                             (~(symbol (str ?struct-name "-" field-name)) obj#))
                           (fn [obj# data#]
                             (assoc obj# ~(keyword (str *ns*) (str ?struct-name "-" field-name)) data#))
                           (fn [obj# f#]
                             (update obj# ~(keyword (str *ns*) (str ?struct-name "-" field-name)) f#))))
              (s/fdef ~(symbol (str ?struct-name "-" field-name))
                :args (s/cat ~(keyword (str ?struct-name)) ~(keyword (str *ns*) (str ?struct-name)))
                :ret ~(keyword (str *ns*) (str ?struct-name "-" field-name))))))

     ;; Predicate
     (defn ~?predicate-name
       [obj#]
       (s/valid? ~(keyword (str *ns*) (str ?struct-name)) obj#))))
