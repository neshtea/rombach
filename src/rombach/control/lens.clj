(ns rombach.control.lens
  (:require [clojure.spec.alpha :as s]))

(defn lens
  [view update over]
  {:_struct     'lens
   ::lens-view   view
   ::lens-update update
   ::lens-over   over})

(s/def ::lens-view fn?)
(s/def ::lens-update fn?)
(s/def ::lens-over fn?)

(s/def ::lens (s/and (s/keys :req [::lens-view ::lens-update ::lens-over])
                     #(= 'lens (:_struct %))))

(defn _view [lens structure] ((::lens-view lens) structure))
(defn _update [lens structure value] ((::lens-update lens) structure value))
(defn _over [lens structure function] ((::lens-over lens) structure function))

;; Lens composition
(defn >>2
  "Composes two lenses together into 1, from left to right."
  [lens-1 lens-2]
  (lens (fn [x]
          (_view lens-2 (_view lens-1 x)))
        (fn [x n]
          (_update lens-1 x (_update lens-2 (_view lens-1 x) n)))
        (fn [x f]
          (_update lens-1 x (_over lens-2 (_view lens-1 x) f)))))

(defn >>
  "Compose a sequence of lenses into 1, from left to right."
  [& lenses]
  (reduce (fn [acc lens] (>>2 acc lens)) (first lenses) (rest lenses)))

;; Lenses over lists.
(defn list-index-lens
  ([i]
   (list-index-lens i nil))
  ([i default]
   (lens (fn [xs] (if (> i (count xs))
                    (throw (ex-info "list index out of bounds" {:arguments [xs i]}))
                    (nth xs i default)))
         (fn [xs n] (if (> i (count xs))
                      (throw (ex-info "list index out of bounds" {:arguments [xs i]}))
                      (map-indexed (fn [idx x] (if (= idx i) n x)) xs)))
         (fn [xs f] (if (> i (count xs))
                      (throw (ex-info "list index out of bounds" {:arguments [xs i]}))
                      (map-indexed (fn [idx x] (if (= idx i) (f x) x)) xs))))))

(defn list!!
  ([i] (list-index-lens i))
  ([i default] (list-index-lens i default)))

;; Lenses over vectors.
(defn vec-index-lens
  ([i]
   (vec-index-lens i nil))
  ([i default]
   (lens (fn [xs] (if (> i (count xs))
                    (throw (ex-info "vector index out of bounds" {:arguments [xs i]}))
                    (nth xs i default)))
         (fn [xs n] (if (> i (count xs))
                      (throw (ex-info "vector index out of bounds" {:arguments [xs i]}))
                      (assoc xs i n)))
         (fn [xs f] (if (> i (count xs))
                      (throw (ex-info "vector index out of bounds" {:arguments [xs i]}))
                      (update xs i f))))))

(defn vec!!
  ([i] (vec-index-lens i))
  ([i default] (vec-index-lens i default)))
