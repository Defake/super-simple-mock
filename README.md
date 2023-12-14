# Super Simple Mock
Tired of libraries written by someone who doesn't know the difference between stubs and mocks? Enjoy!

### Usage

```clojure
(defn test-mocked-fn [& args]
  (apply + 100 args))

(defn test-wrapped-fn [& args]
  (apply + 100 args))

(defn test-tracked-fn [& args]
  (apply + 100 args))

(defn test-stubbed-fn [& args]
  (apply + 100 args))

(with-mocks
 [:mock [test-mocked-fn str]
  :wrap [test-wrapped-fn str]
  :track [test-tracked-fn]
  :stub [test-stubbed-fn 421]]

 (prn (test-mocked-fn 1 2 3)) ;; => "123"
 (prn (test-wrapped-fn 1 2 3)) ;; => "106"
 (prn (test-tracked-fn 1 2 3)) ;; => 106
 (prn (test-stubbed-fn 1 2 3)) ;; => 421
 (prn (get-in @calls* ['test-mocked-fn 0])) ;; => [1 2 3]
 (prn (get-in @calls* ['test-wrapped-fn 0])) ;; => [1 2 3]
 (prn (get-in @calls* ['test-tracked-fn 0])) ;; => [1 2 3]
 (prn (get-in @calls* ['test-stubbed-fn 0]))) ;; => [1 2 3]
```

