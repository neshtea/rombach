(ns rombach.control.monad
  (:require [clojure.spec.alpha :as s]
            [rombach.control.applicative :as applicative]
            [rombach.data.functor :as functor]
            [rombach.structure.product :refer [defproduct]]))

(defproduct monad make-monad monad?
  [[applicative ::applicative/applicative]
   [return fn?]
   [bind fn?]
   [seq (s/or :none nil? :function fn?)]
   [fail (s/or :none nil? :function fn?)]])

(defn monad
  ([applicative return bind]
   (monad applicative return bind {:seq  #(throw (Exception. "Not yet implemented."))
                                   :fail #(throw (Exception. "Not yet implemented."))}))
  ([applicative return bind more]
   (make-monad applicative return bind (:seq more) (:fail more))))

(defn _return
   [monad x]
   (when-not (monad? monad)
     (throw (ex-info "not a monad" {:arguments [monad]})))
   ((monad-return monad) x))

(defn _bind
  [monad a b]
  (when-not (monad? monad)
    (throw (ex-info "not a monad" {:arguments [monad]})))
  ((monad-bind monad) a b))

(defn _seq
  [monad a b]
  (when-not (monad? monad)
    (throw (ex-info "not a monad" {:arguments [monad]})))
  ((monad-seq monad) a b))

(defn _fail
  [monad x]
  (when-not (monad? monad)
    (throw (ex-info "not a monad" {:arguments [monad]})))
  ((monad-fail monad) x))


;;;; Monad do
(defmacro monadic-1
  [?implementation ?meta & ?stmts]
  (if (empty? ?stmts)
    (throw (ex-info (str "there must be at least one statement in " *ns* " " ?meta)))
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
             (throw (ex-info (str "let statement must have exactly one subform in "
                                  *ns* " " ?meta))))
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
  This implementation is a staight rip-off from: https://github.com/active-group/active-clojure/blob/master/src/active/clojure/monad.cljc"
  [?monad & ?stmts]
  `(monadic-1 ~?monad ~(meta &form) ~@?stmts))

;;;; Instances
(def list-monad
  (monad applicative/list
         list
         (fn [xs f]
           (apply concat (functor/_fmap (applicative/applicative-functor applicative/list) f xs)))
         {:fail (constantly [])}))
