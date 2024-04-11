(ns super-simple-mock.core
  (:require [clojure.test :refer :all]))

(def calls* (atom {}))

(defmacro mock-fn [[fn-sym override-fn] & body]
  `(do
     (reset! calls* {})
     (with-redefs [~fn-sym (fn [& args#]
                             (swap! calls*
                                    (fn [calls#]
                                      (update calls# (quote ~fn-sym)
                                              #(-> (vec %)
                                                   (conj (vec args#))))))
                             (apply ~override-fn args#))]
       ~@body)))

(comment
 (defn test-mocked-fn [& args]
   (apply + 100 args))

 (mock-fn
  [test-mocked-fn str]

  (prn "result" (test-mocked-fn 1)) ;; => "1"
  (prn "result2" (test-mocked-fn 1 200)) ;; => "1200"
  (prn (get-in @calls* ['test-mocked-fn 0])) ;; => [1]
  (prn (get-in @calls* ['test-mocked-fn 1])))) ;; => [1 200]


(defmacro wrap-fn [[fn-sym convert-result-fn] & body]
  `(let [original-fn# ~fn-sym
         override-fn# (fn [& args#]
                        (let [result# (apply original-fn# args#)]
                          (~convert-result-fn result#)))]
     (mock-fn
      [~fn-sym override-fn#]
      ~@body)))

(comment
 (defn test-wrapped-fn [& args]
   (apply + 100 args))

 (wrap-fn
  [test-wrapped-fn str]

  (prn "result" (test-wrapped-fn 1)) ;; => "101"
  (prn "result2" (test-wrapped-fn 1 200)) ;; => "301"
  (prn (get-in @calls* ['test-wrapped-fn 0])) ;; => [1]
  (prn (get-in @calls* ['test-wrapped-fn 1])))) ;; => [1 200]


(defmacro track-fn [[fn-sym] & body]
  `(wrap-fn
    [~fn-sym identity]
    ~@body))

(comment
 (defn test-tracked-fn [& args]
   (apply + 100 args))

 (track-fn
  [test-tracked-fn]

  (prn "result" (test-tracked-fn 1)) ;; => 101
  (prn "result2" (test-tracked-fn 1 200)) ;; => 301
  (prn (get-in @calls* ['test-tracked-fn 0])) ;; => [1]
  (prn (get-in @calls* ['test-tracked-fn 1])))) ;; => [1 200]


(defmacro stub-fn [[fn-sym value] & body]
  `(mock-fn
    [~fn-sym (fn [& args#]
               ~value)]
    ~@body))

(comment
 (defn test-stubbed-fn [& args]
   (apply + 100 args))

 (stub-fn
  [test-stubbed-fn 421]

  (prn "result" (test-stubbed-fn 1)) ;; => 421
  (prn "result2" (test-stubbed-fn 1 200)) ;; => 421
  (prn (get-in @calls* ['test-stubbed-fn 0])) ;; => [1]
  (prn (get-in @calls* ['test-stubbed-fn 1])))) ;; => [1 200]


(defmacro with-mocks [mocks & body]
  (let [mocks (if (symbol? mocks)
                @(resolve mocks)
                mocks)]
    (assert (vector? mocks))
    (assert (zero? (mod (count mocks) 2)))

    (->> mocks
         (partition-all 2)
         (reduce (fn [form mock-vec]
                   (let [[mock-type args] mock-vec]
                     (case mock-type
                       :mock `(mock-fn ~args ~form)
                       :wrap `(wrap-fn ~args ~form)
                       :track `(track-fn ~args ~form)
                       :stub `(stub-fn ~args ~form))))
                 `(do ~@body)))))

(comment
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

 ;; If you put your mocks into a variable, you need to quote the mocked functions symbols
 (def mocks-var [:mock ['test-mocked-fn str]])

 (with-mocks
  mocks-var
  (prn (test-mocked-fn 1 2 3)) ;; => "123"
  (prn (get-in @ssm/calls* ['test-mocked-fn 0])))) ;; => [1 2 3]
