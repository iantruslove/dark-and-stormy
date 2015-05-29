(ns dark-and-stormy.test.components.webserver
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.api :as api]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.test.helpers :refer [with-system]])
  (:import (java.util Date)))

(deftest test-metrics-middleware
  (let [handler identity
        ;; Doesn't really matter what the metrics component is for this test
        metrics-component (Date.)
        wrapped-handler (webserver/wrap-metrics-component handler metrics-component)]
    (is (= metrics-component
           (:metrics (wrapped-handler {:foo :bar}))))))

(defrecord FakeMetrics [config]
  ;; No need to implement component/Lifecycle, since Object already
  ;; has a stub implementation.
  )

(deftest test-running-webserver
  (with-system [sys (-> (component/system-map
                         :config (config/map->Config {})
                         :webserver (webserver/new #'api/routes)
                         :metrics (map->FakeMetrics {}))
                        (component/system-using
                         {:webserver [:config :metrics]
                          :metrics [:config]}))]
    (is (= 200 (-> (str "http://localhost:"
                        (config/config (:config sys) :webserver :port))
                   http/get
                   :status)))))
