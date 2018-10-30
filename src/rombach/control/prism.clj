(ns rombach.control.prism
  (:require [rombach.structure.product :refer [defproduct]]))

(defproduct prism prism prism?
  [[match fn?]
   [build fn?]])

(defn _match [prism structure] ((prism-match prism) structure))
(defn _build [prism value] ((prism-build prism) value))
