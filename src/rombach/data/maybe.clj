(ns rombach.data.maybe
  (:require [active.clojure.condition :as c]
            [clojure.spec.alpha :as s]
            [rombach.control.applicative :as applicative]
            [rombach.data.either :as either]
            [rombach.data.functor :as functor]
            [rombach.control.monad :as monad]
            [rombach.data.monoid :as monoid]
            [rombach.control.prism :as prism]
            [rombach.strcture.product :refer [defproduct]]))

;;;; Maybe
(defproduct just just just?
  [[a any?]])

(defproduct nothing make-nothing nothing?
  [])

(def nothing (make-nothing))

(defn maybe?
  [obj]
  (or (just? obj) (nothing? obj)))

(s/def ::maybe (s/or :just ::just
                     :nothing ::nothing))

;;;; Prism over maybe.
(def the-just
  (prism/prism (fn [m]
                 (cond
                   (just? m)    (either/right (just-a m))
                   (nothing? m) (either/left nothing)))
               just))

(def the-nothing
  (prism/prism (fn [m]
                 (cond
                   (just? m)    (either/left m)
                   (nothing? m) (either/right nothing)))
               (constantly nothing)))

;;;; Utility functions.

(defn- fail-maybe
  [sym & ms]
  (apply c/assertion-violation sym "not a value of type maybe" ms))

(defn maybe
  "The maybe function takes a default value, a function, and a Maybe value.
  If the Maybe value is Nothing, the function returns the default value.
  Otherwise, it applies the function to the value inside the Just and returns
  the result."
  [b a->b m-a]
  (cond
    (just? m-a)    (a->b (just-a m-a))
    (nothing? m-a) b
    :else          (fail-maybe `maybe m-a)))

(defn from-just
  [m-a]
  (cond
    (just? m-a)    (just-a m-a)
    (nothing? m-a) (c/assertion-violation `from-just "not a value of type just" m-a)
    :else          (fail-maybe `from-just m-a)))

(defn from-maybe
  "The from-just function extracts the element out of a Just and throws an error
  if its argument is Nothing."
  [a m-a]
  (cond
    (just? m-a)    (just-a m-a)
    (nothing? m-a) a
    :else          (fail-maybe `from-maybe m-a)))

(defn list-to-maybe
  "The list-to-maybe function returns Nothing on an empty list or Just a where
  a is the first element of the list."
  [as]
  (cond
    (empty? as) nothing
    (list? as)  (just (first as))
    :else       (c/assertion-violation `list-to-maybe "not a list" as)))

(defn maybe-to-list
  "The maybeToList function returns an empty list when given Nothing or a
  singleton list when not given Nothing."
  [m-a]
  (cond
    (nothing? m-a) '()
    (just? m-a) (list (just-a m-a))))

(defn cat-maybes
  "The cat-maybes function takes a list of Maybes and returns a list of all the
  Just values."
  [m-as]
  (filter just? m-as))

(defn map-maybe
  "The map-maybe function is a version of map which can throw out elements.
  In particular, the functional argument returns something of type Maybe b.
  If this is Nothing, no element is added on to the result list.
  If it is Just b, then b is included in the result list."
  [a->m-b as]
  (->> as
       (map a->m-b)
       (filter just?)
       (map just-a)))

;;;; Typeclass implementations
(def functor
  "Functor instance for values of type maybe."
  (functor/functor
   (fn [f m]
     (cond
       (just? m)    (just (f (just-a m)))
       (nothing? m) m
       :else        (fail-maybe `functor m)))))

(def applicative
  "Applicative instance for values of type maybe."
  (applicative/applicative
   functor
   just
   (fn [f m]
     (cond
       (nothing? f) f
       (just? f)    ((functor/functor-fmap functor) (just-a f) m)
       :else        (fail-maybe `applicative m)))))

(defn monoid-of
  [inner-monoid]
  (monoid/monoid
   nothing
   (fn [ma mb]
     (cond
       (nothing? ma) mb
       (nothing? mb) ma
       (and (just? ma) (just? mb))
       (just (monoid/monoid-mappend inner-monoid (just-a ma) (just-a mb)))
       :else         (fail-maybe `maybe-monoid-of ma mb)))))

(def monad
  (monad/monad
   applicative
   just
   (fn [fa fb]
     (cond
       (nothing? fa) fa
       (just? fa)    (fb (just-a fa))
       :else         (fail-maybe `maybe-monad fa fb)))))
