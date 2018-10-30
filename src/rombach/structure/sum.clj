(ns rombach.structure.sum
  (:require [rombach.structure.product :refer [defproduct]]
            [clojure.spec.alpha :as s]))

(defmacro defsum
  [?sum-name ?predicate-name ?predicate-pairs]
  `(do
     (def ~?sum-name {:predicates ~(map first ?predicate-pairs)})
     ;; Predicate
     (s/def ~(keyword (str *ns*) (str ?sum-name))
       (s/or ~@(apply concat
                      (map (fn [[pred-sym pred-spec]]
                             [(keyword pred-sym) pred-spec])
                           ?predicate-pairs))))
     (defn ~?predicate-name [obj#]
       (s/valid? ~(keyword (str *ns*) (str ?sum-name)) obj#))))

(defn list-contains?
  [xs x]
  (reduce (fn [acc x] (or acc (= (first xs) x))) false xs))

(defn list-remove
  [xs x]
  (filter #(not= % x) xs))


(defmacro match
  [?sum-type ?data & ?clauses]
  (let [?cond-clauses (->> ?clauses
                           (partition 2)
                           (mapv (fn [[lhs rhs]] `((~lhs ~?data) ~rhs)))
                           (apply concat))
        ?predicates   (:predicates (eval ?sum-type))]
    `(do
       ;; 1. check if the predicate is part of the sum type
       ;; 2. check if all predicates are matched
       ~(let [missing
              (reduce (fn [acc ?p]
                        (println acc ?p)
                        (if (or (= ?p :else) (list-contains? acc (eval ?p)))
                          (list-remove acc (eval ?p))
                          ;; If we encounter a predicate we've encountered before, it's unreachable.
                          ;; We want to notify about this seperately.
                          (if (list-contains? ?predicates (eval ?p))
                            (throw (ex-info "unreachable predicate" {:argument ?p}))
                            (throw (ex-info "predicate note part of sum type" {:argument   ?p
                                                                               :sum-type   ?sum-type
                                                                               :predicates (:predicate (eval ?sum-type))})))))
                      ?predicates
                      (map first (partition 2 ?clauses)))]
          (when-not (empty? missing)
            (throw (ex-info "incomplete match" {:missing-clauses missing}))))
       (cond
         ~@?cond-clauses))))
