# rombach

A magic-free, side-effect-free, atom-free Clojure library that implements some of the well known data-structures from Haskell (e.g. Maybe, Either, Functor, Applicative, Monad, Monoid, more to come?).

It ships with several implementations of functor, applicative and monad for lists, vectors, maybe, either, ...

## Usage

### Conventions

To use the functionality provided here, it is useful to know a little bit about 
it's conventions (which of course are still in flux).
For most of the data-structures, this project's structure mimics that of the
equivalent Haskell packages they implement (e.g. `data.Maybe` in Haskell is found
in `rombach.data.monoid`, etc.).

Each namespace implements exactly one structure and defines implementations for
primitive Clojure strctures when applicable.
For example, `rombach.data.monoid` defines:

1. The typeclass `monoid`.
2. The typeclass functions, that is `_mempty` and `_mappend`.
3. Implementations of `monoid` for `list`s and `vector`s.

The name of the implementations that are defined within the namespace of i.e. 
`rombach.data.monoid` have the type name (i.e. `list`), prefixed by an underscore.

A namespace should always be required with a qualifier, that is
`(:require [rombach.data.monoid :as monoid])`.
This allows for descriptive names in each namespace that match everywhere.
For example the functor implementation for lists (`rombach.data.functor/_list`)
has the same name as it's applicative implementation (`rombach.control.applicative/_list`).

I think this makes the intent clear and allows for nicer code..

As every implementation of any typeclass is just data, you are always free the
bind it to another name (there is absolutely no magic here, just data and functions).

For types that are not built in the Clojure language (at this point `rombach`
provides `rombach.data.either` and `rombach.data.maybe`), the convention is that
typeclass implementations are done in the same namespace as the data structure
and have the same name as the typeclass they are implementing.
This means that, for example, the functor instance for maybe is found at
`rombach.data.maybe/functor`, same for applicative (`rombach.data.maybe/applicative`).

### Semi-Group

Values that implement semi-group can be concatenated.
A data-type that implements semi-group implements a function `_cat :: a -> a -> a`.

Examples:

```clojure
(ns rombach.playground
  (:require [rombach.data.semi-group :as sg]))

(sg/_cat sg/_list '(1 2 3) '(4 5))  ;; => '(1 2 3 4 5)
```

To implement a semi-group, you just have to define an associative cat function 
(that is: (a _cat b) _cat c == a _cat (b _cat c)).

So, to define a semi-group for vectors, you could do the following:

```clojure
(def vector-semi-group
  (sg/semi-group (fn [xs ys]
                   (into [] (concat xs ys)))))


(sg/_cat vector-semi-group [1 2 3] [4 5 6])
```

You need to do your type-checking yourself.
This also means that you have full control over your implementation and what
you want to be vali or not.
The vector-semi-group is implemented as `rombach.data.semi-group/_vec`.

### Monoid

Values that implement monoid are semi-groups that have a zero element.
A data-type that implements monoid implements a function `_mappend :: a -> a -> a`.
To define a monoid, you first have to define a semi-group.
Afterwards, if you notice that your semi-group has a zero element, you can define
the monoid in terms of the semi-group plus the zero (that element is called the `_mempty`).

Examples:

```clojure
(ns rombach.playground
  (:require [rombach.data.monoid :as mon]))

(mon/_mappend mon/_list '(1 2 3) '(4 5))  ;; => '(1 2 3 4 5)
(mon/_mempty mon/_list)  ;; => '()
```

To implement a monoid, you have to define a semi-group and supply the zero element.
So, to define a monoid for vectors, using the vector semi-group from above, 
you could do the following (using `[]` as the zero element).

```clojure
(def vector-semi-group
  (sg/semi-group (fn [xs ys]
                   (into [] (concat xs ys)))))

(def vector-monoid
  (mon/monoid vector-semi-group []))

(mon/_mempty vector-monoid)  ;; => []
(mon/_mappend vector-monoid [1 2 3] [4 5])  ;; => [1 2 3 4 5]
(mon/_mappend vector-monoid [] [4 5])  ;; => [4 5]
(mon/_mappend vector-monoid [1 2 3] [])  ;; => [1 2 3]
```

You need to do your type-checking yourself.
This also means that you have full control over your implementation and what
you want to be vali or not.
The vector-monoid is implemented as `rombach.data.monoid/_vec`.

### Functor

A functor is a data value that implements a function `fmap :: (a -> b) -> a -> b`.
Im `rombach`, functors are implemented as a simple datastructure, defined in `rombach.functor` and provides functors for list, vector, set, hash-map, either and maybe.

Examples:

```clojure
(ns rombach.playground
  (:require [rombach.data.functor :as f]
            [rombach.data.maybe :as m]))

;; Functor for lists.
(f/_fmap f/_list inc '())  ;; => '()
(f/_fmap f/_list inc '(1 2 3))  ;; => '(2 3 4)

;; Functor for vectors.
(f/_fmap f/_vec inc [])  ;; => []
(f/_fmap f/_vec inc [1 2 3])  ;; [2 3 4]

;; Functor for maybe.
(f/_fmap m/functor inc (m/just 41))  ;; => (m/just 42)
(f/_fmap m/functor inc m/nothing)  ;; => m/nothing
```

`rombach` respects the type of value you provide (i.e. the list-functor only accepts values of type list.)

```clojure
(f/_fmap  f/_list inc [1 2 3])
;; =>
;; 1.Unhandled clojure.lang.ExceptionInfo
;;   not a value of type list
;;   {:arguments [[1 2 3]]}
```


## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
