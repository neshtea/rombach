(ns rombach.control.monad
  (:require [clojure.spec.alpha :as s]
            [rombach.control.applicative :as applicative]
            [rombach.data.functor :as functor]
            [active.clojure.record :refer [define-record-type]]
            [active.clojure.condition :as c]))

(define-record-type Monad
  (make-monad applicative return bind seq fail) monad?
  [applicative monad-applicative
   return monad-return
   bind monad-bind
   seq monad-seq
   fail monad-fail])

(defn monad
  ([applicative return bind]
   (monad applicative return bind {:seq  #(c/assertion-violation `monad "No implementation for monad-seq")
                                   :fail #(c/assertion-violation `monad "No implementation for monad-fail")}))
  ([applicative return bind more]
   (make-monad applicative return bind (:seq more) (:fail more))))

(defn _return
   [monad x]
   (when-not (monad? monad)
     (c/assertion-violation `_return "not a monad" monad))
   ((monad-return monad) x))

(defn _bind
  [monad a b]
  (when-not (monad? monad)
    (c/assertion-violation `_bind "not a monad" monad))
  ((monad-bind monad) a b))

(defn _seq
  [monad a b]
  (when-not (monad? monad)
    (c/assertion-violation `_seq "not a monad" monad))
  ((monad-seq monad) a b))

(defn _fail
  [monad x]
  (when-not (monad? monad)
    (c/assertion-violation `_fail "not a monad" monad))
  ((monad-fail monad) x))

;;;; Monad do
(defmacro monadic-1
  [?implementation ?meta & ?stmts]
  (if (empty? ?stmts)
    (c/assertion-violation `monadic-1 "there must be at least one statement in " *ns* " " ?meta)
    (let [?stmt (first ?stmts)]
      (cond
       (vector? ?stmt)
       (do
         (letfn [(recurse [?pairs]
                   (let [[?pat ?rhs] (first ?pairs)
                         ?rest (rest ?pairs)]
                     `(with-meta (_bind ~?implementation
                                        ~?rhs
                                        (fn [~?pat]
                                          ~(if (empty? ?rest)
                                             `(monadic-1 ~?implementation ~?meta ~@(rest ?stmts))
                                             (recurse ?rest))))
                        '~(assoc ?meta :statement [?pat ?rhs]))))]
           (recurse (partition 2 ?stmt))))

       (and (list? ?stmt)
            (= 'let (first ?stmt)))
       (do (when-not (= 2 (count ?stmt))
             (c/assertion-violation `monadic-1 "let statement must have exactly one subform in " *ns* " " ?meta))
           `(let ~(second ?stmt)
              (monadic-1 ~?implementation ~?meta ~@(rest ?stmts))))

       (empty? (rest ?stmts))
       `(vary-meta ~(first ?stmts)
          ~'assoc :statement '~(first ?stmts))

       :else
       `(with-meta (_bind ~?implementation ~?stmt (fn [_#] (monadic-1 ~?implementation ~?meta ~@(rest ?stmts))))
          '~(assoc ?meta :statement ?stmt))))))

(defmacro monadic
  "CREDIT to active.clojure.monad.
  This implementation is almost a staight rip-off from:
  https://github.com/active-group/active-clojure/blob/master/src/active/clojure/monad.cljc"
  [?monad & ?stmts]
  `(monadic-1 ~?monad ~(meta &form) ~@?stmts))

;;;; Instances
(def _list
  (monad
   applicative/_list
   list  ; return
   (fn [xs f]  ; bind
     (apply concat
            (functor/_fmap
             (applicative/applicative-functor applicative/_list) f xs)))
   {:fail (constantly [])}))
