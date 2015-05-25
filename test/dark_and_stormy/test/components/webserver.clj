(ns dark-and-stormy.test.components.webserver
  (:require [dark-and-stormy.components.webserver :refer :all]
            [clojure.test :refer :all])
  (:import (java.util Date)))

(deftest test-metrics-middleware
  (let [handler identity
        metrics-component (Date.) ;; Doesn't really matter what the
                                  ;; metrics component is for this test
        wrapped-handler (wrap-metrics-component handler metrics-component)]
    (is (= metrics-component
           (:metrics (wrapped-handler {:foo :bar}))))))
