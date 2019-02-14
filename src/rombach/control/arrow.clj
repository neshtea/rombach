(ns rombach.control.arrow
  (:refer-clojure :exclude [first second])
  (:require [active.clojure.record :refer [define-record-type]]
            [rombach.control.category :as cat]))

(define-record-type Arrow
  (arrow category arr first) arrow?
  [category arrow-category
   arr arrow-arr
   first arrow-first])

(defn arr
  "Lift a function to an arrow."
  [arrow-impl f]
  ((arrow-arr arrow-impl) f))

(defn >>>
  "Left-to-right composition."
  [arrow-impl f g]
  (cat/>>> (arrow-category arrow-impl) f g))

(defn >>>*
  [arrow-impl f g & hs]
  (reduce (fn [acc f'] (cat/<<< (arrow-category arrow-impl) f' acc)) (cat/>>> (arrow-category arrow-impl) f g) hs))

(defn <<<
  "Right-to-left composition."
  [arrow-impl f g]
  (cat/<<< (arrow-category arrow-impl) f g))

(defn first
  "Send the first component of the input through the argument arrow, and copy
  the rest unchanged to the output."
  [arrow-impl f]
  ((arrow-first arrow-impl) f))

(defn swap [[a b]] [b a])

(defn split
  "Split the input between the two argument arrows and combine their output.
  Note that this is in general not a functor."
  [arrow-impl f g]
  (>>> arrow-impl
       (>>> arrow-impl
            (>>> arrow-impl
                 (first arrow-impl f)
                 (arr arrow-impl swap))
            (first arrow-impl g))
       (arr arrow-impl swap)))

(defn fanout
  [arrow-impl f g]
  (>>> arrow-impl
       (arr arrow-impl (fn [b] [b b]))
       (split arrow-impl
              f
              g)))

(defn second
  "A mirror image of [[_first]]."
  [arrow-impl f]
  (split arrow-impl identity f))
