(ns rombach.core
  (:require [rombach.functor :as functor]
            [rombach.applicative :as applicative]
            [active.clojure.condition :as c]
            [rombach.monoid :as monoid]
            [rombach.monad :as monad]))

(defn _fmap
  [functor a->b fa]
  (cond
    (functor/functor? functor)
    ((functor/functor-fmap functor) a->b fa)

    (applicative/applicative? functor)
    ((functor/functor-fmap (applicative/applicative-functor functor)) a->b fa)

    :else (c/assertion-violation `_fmap "not a functor" functor)))

(defn _pure
  "Takes an applicative and some value x and 'lifts' it into the applicative."
  [applicative x]
  (when-not (applicative/applicative? applicative)
    (c/assertion-violation `_pure "not an applicative" applicative))
  ((applicative/applicative-pure applicative) x))

(defn _apply
  "Takes an applicative, an applicative `f` and an applicative `x` of that
  applicative type and applicative-applies `f` to `x`."
  [applicative f x]
  (when-not (applicative/applicative? applicative)
    (c/assertion-violation `_pure "not an applicative" applicative))
  ((applicative/applicative-apply applicative) f x))

(defn _mempty
  "Takes a monoid and returns the zero element of that monoid."
  [monoid]
  (when-not (monoid/monoid? monoid)
    (c/assertion-violation `_mempty "not a monoid" monoid))
  (monoid/monoid-mempty monoid))

(defn _mappend
  "Takes a monoid and two values of that monoid and appends them."
  [monoid a b]
  (when-not (monoid/monoid? monoid)
    (c/assertion-violation `_mappend "not a monoid" monoid))
  ((monoid/monoid-mappend monoid) a b))

(defn _return
  [monad x]
  (when-not (monad/monad? monad)
    (c/assertion-violation `_return "not a monad" monad))
  ((monad/monad-return monad) x))

(defn _bind
  [monad a b]
  (when-not (monad/monad? monad)
    (c/assertion-violation `_bind "not a monad" monad))
  ((monad/monad-bind monad) a b))

(defn _seq
  [monad a b]
  (when-not (monad/monad? monad)
    (c/assertion-violation `_seq "not a monad" monad))
  ((monad/monad-seq monad) a b))

(defn _fail
  [monad x]
  (when-not (monad/monad? monad)
    (c/assertion-violation `_fail "not a monad" monad))
  ((monad/monad-fail monad) x))

;;;; Functor instances for some types.
(def list-functor
  (functor/functor
   (fn [f xs]
     (when-not (list? xs)
       (c/assertion-violation `list-functor "not a list" xs))
     (map f xs))))

(def vec-functor
  (functor/functor
   (fn [f xs]
     (when-not (vector? xs)
       (c/assertion-violation `vec-functor "not a vector" xs))
     (mapv f xs))))

(def fn-functor
  (functor/functor (fn [f g]
                     (fn [x]
                       (f (g x))))))

;;;; Applicative instances for some types.
(def list-applicative
  (applicative/applicative
   list-functor
   list
   (fn [fs xs]
     (for [f fs
           x xs]
       (f x)))))

(def vec-applicative
  (applicative/applicative
   vec-functor
   list
   (fn [fs xs]
     (into [] (for [f fs
                    x xs]
                (f x))))))

(def fn-applicative
  (applicative/applicative
   fn-functor
   (fn [x] (fn [_] x))
   (fn [f g]
     (fn [x]
       (f x (g x))))))

(defn liftA-2
  [applicative f a b]
  (_apply applicative
          (_fmap applicative f a)
          b))

(defn sequenceA
  [applicative a]
  (if (empty? a)
    (_pure applicative '())
    (_apply applicative
     (_fmap applicative
            (fn [x] #(cons x %))
            (first a))
     (sequenceA applicative (rest a)))))

(def list-monoid
  (monoid/monoid [] concat))

(def list-monad
  (monad/monad list-applicative
               list
               (fn [xs f]
                 (apply concat (_fmap list-applicative f xs)))
               {:fail (constantly [])}))

(defmacro monadic-1
  [?implementation ?meta & ?stmts]
  (if (empty? ?stmts)
    (c/assertion-violation `monadic-1 (str "there must be at least one statement in " *ns* " " ?meta))
    (let [?stmt (first ?stmts)]
      (cond
       (vector? ?stmt)
       (do
         (letfn [(recurse [?pairs]
                   (let [[?pat ?rhs] (first ?pairs)
                         ?rest (rest ?pairs)]
                     `(with-meta (rom/_bind ~?implementation
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
             (c/assertion-violation `monadic-1 (str "let statement must have exactly one subform in "
                                                    *ns* " " ?meta)))
           `(let ~(second ?stmt)
              (monadic-1 ~?implementation ~?meta ~@(rest ?stmts))))

       (empty? (rest ?stmts))
       `(vary-meta ~(first ?stmts)
          ~'assoc :statement '~(first ?stmts))

       :else
       `(with-meta (_bind ~?implementation ~?stmt (fn [_#] (monadic-1 ~?implementation ~?meta ~@(rest ?stmts))))
          '~(assoc ?meta :statement ?stmt))))))

(defmacro monadic
  "Construct a monadic computation.

  The syntax is `(monadic <stmt> ...)` where `<stmt>` is one of the following:

  - `[<pat> <exp> ...]` which creates monadic bindings
  - `(let <bindings>)` which creates regular bindings
  - anything else is just a regular expression, expected to yield a monadic value.

  Example:

      (monadic [first (ask \"what's your first name?\")
                last (ask \"what's your last name?\")]
               (let [s (str \"Hello, \" first \" \" last)])
               (tell s))"
  [?monad & ?stmts]
  `(monadic-1 ~?monad ~(meta &form) ~@?stmts))

