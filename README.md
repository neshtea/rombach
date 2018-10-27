# rombach

A Clojure library that implements some of the well known data-structures from Haskell (e.g. Maybe, Either, Functor, Applicative, Monad, Monoid, more to come?).

It ships with several implementations of functor, applicative and monad for lists, vectors, maybe, either, ...

## Usage

Most of the relevant functions are defined in `rombach.core`.

### Functor

A functor is a data value that implements a function `fmap :: (a -> b) -> a -> b`.
Im `rombach`, functors are implemented as a simple datastructure, defined in `rombach.functor` and provides functors for list, vector, set, hash-map, either and maybe.

Examples:

```clojure
(ns some.name.space
  (:require [rombach.core :as rom]
            [rombach.maybe :as maybe]))

(_fmap list-functor inc '())  ;; => '()
(_fmap list-functor inc (list 1 2 3))  ;; => (list 2 3 4)
(_fmap vec-functor inc [1 2 3])  ;; => [2 3 4]
(_fmap maybe/functor inc (maybe/just 42))  ;; => (just 43)
(_fmap maybe/functor inc maybe/nothingA  ;; => nothing)
```

`rombach` respects the type of value you provide (i.e. the list-functor only accepts values of type list.)

```clojure
(_fmap list-functor inc [1 2 3])
;; => 1. Unhandled clojure.lang.ExceptionInfo
;;   not a value of type list
```


## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
