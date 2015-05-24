(ns dark-and-stormy.test.util.rate-limit
  (:require [dark-and-stormy.util.rate-limit :refer :all]
            [clojure.test :refer :all]))

(deftest test-removing-oldest-items-from-list
  (with-redefs [now-millis (constantly 1000)]
    (is (= 999 (millis-ago 1)))
    (let [millis-coll [800 900 1000 1100]]
      (is (= [1000 1100] (remove-oldest 1000 millis-coll)))
      (is (= [1000 1100] (remove-older-than 0 millis-coll)))
      (is (= [1000 1100] (remove-older-than 99 millis-coll)))
      (is (= [900 1000 1100] (remove-older-than 100 millis-coll))))))

(deftest test-rate-limiting
  (let [a (atom 0)
        call-counter (fn [] (swap! a inc))
        f (rate-limit call-counter 4 100)]
    (is (f))
    (is (f))
    (is (f))
    (is (f))
    (try
      (f)
      (is false "Should not reach this")
      (catch Exception _))
    (Thread/sleep 100)
    (is (= 5 (f)))))
