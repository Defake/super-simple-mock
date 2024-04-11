# Super Simple Mock
Tired of libraries written by someone who doesn't know the difference between stubs and mocks? Enjoy!

### Mock types
- Track – doesn't replace a function, just records its usages to ssm/calls*
- Stub – replaces a function with an fn that accepts any number of arguments and returns a constant. Records its usages to ssm/calls*
- Wrap – wraps a function result with a provided wrapper. Records its usages to ssm/calls*
- Mock - replaces a function with a provided override. Records its usages to ssm/calls*

### Usage

```edn
{:deps {super-simple-mock/super-simple-mock {:git/url "https://github.com/Defake/super-simple-mock"
                                             :sha "f88cf399233c42e8c5abc360609aa84579de1d5c"}}}
```

```clojure
(require '[super-simple-mock.core :as ssm])

(defn test-mocked-fn [& args]
  (apply + 100 args))

(defn test-wrapped-fn [& args]
  (apply + 100 args))

(defn test-tracked-fn [& args]
  (apply + 100 args))

(defn test-stubbed-fn [& args]
  (apply + 100 args))

(ssm/with-mocks
 [:mock [test-mocked-fn str]
  :wrap [test-wrapped-fn str]
  :track [test-tracked-fn]
  :stub [test-stubbed-fn 421]]

 (prn (test-mocked-fn 1 2 3)) ;; => "123"
 (prn (test-wrapped-fn 1 2 3)) ;; => "106"
 (prn (test-tracked-fn 1 2 3)) ;; => 106
 (prn (test-stubbed-fn 1 2 3)) ;; => 421
 (prn (get-in @ssm/calls* ['test-mocked-fn 0])) ;; => [1 2 3]
 (prn (get-in @ssm/calls* ['test-wrapped-fn 0])) ;; => [1 2 3]
 (prn (get-in @ssm/calls* ['test-tracked-fn 0])) ;; => [1 2 3]
 (prn (get-in @ssm/calls* ['test-stubbed-fn 0]))) ;; => [1 2 3]

;; If you put your mocks into a variable, you need to quote the mocked functions symbols
(def mocks-var [:mock ['test-mocked-fn str]])

(with-mocks
 mocks-var
 (prn (test-mocked-fn 1 2 3)) ;; => "123"
 (prn (get-in @ssm/calls* ['test-mocked-fn 0]))) ;; => [1 2 3]

```

