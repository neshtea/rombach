(ns rombach.control.category
  (:require [active.clojure.record :refer [define-record-type]]))

(define-record-type Category
  (category id comp) category?
  [id category-id
   comp category-comp])

(def function-category
  (category identity comp))

(defn id
  [category-impl x]
  ((category-id category-impl) x))

(defn <<<
  "Right-to-left composition."
  [category-impl f g]
  ((category-comp category-impl) f g))

(defn >>>
  "Left-to-right composition"
  [category-impl f g]
  ((category-comp category-impl) g f))
