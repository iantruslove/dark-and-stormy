(ns dark-and-stormy.test.components.api
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.api :refer :all]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.geolocation :as geo]
            [dark-and-stormy.test.helpers :refer [with-system]])
  (:import (java.util Date)))

(defrecord StubMetrics [config last-sent-metric]
  ;; No need to implement component/Lifecycle, since Object already
  ;; has a stub implementation.
  metrics/MetricsService
  (send [this metric-type data]
    (reset! (:last-sent-metric this) data)))

(deftest test-ip-address-override
  (let [last-sent-metric (atom nil)]
    (with-system [sys (-> (component/system-map
                           :api (map->Api {})
                           :config (config/map->Config {})
                           :metrics (map->StubMetrics {:last-sent-metric last-sent-metric})
                           :webserver (webserver/map->JettyWebserver {}))
                          (component/system-using
                           {:api [:metrics]
                            :metrics [:config]
                            :webserver [:api :config :metrics]}))]
      (let [base-url (str "http://localhost:" (config/config (:config sys) :webserver :port))]
        (testing "x-remote-addr-override"
          (http/get (str base-url "/login?username=foo@password=bar")
                    {:throw-exceptions false
                     :headers {"x-remote-addr-override" "4.53.74.173"
                               "x-forwarded-for" "1.2.3.4"}})
          (is (= "4.53.74.173" (:ip @last-sent-metric))))
        (testing "x-forwarded-for - e.g. for Heroku"
          (http/get (str base-url "/login?username=foo@password=bar")
                    {:throw-exceptions false
                     :headers {"x-forwarded-for" "1.2.3.4"}})
          (is (= "1.2.3.4" (:ip @last-sent-metric))))))))

(defrecord NullMetrics [])

(deftest test-component-middleware
  (with-system [sys (-> (component/system-map
                         :metrics (->NullMetrics)
                         :api (map->Api {}))
                        (component/system-using
                         {:api [:metrics]}))]
    (with-redefs [dark-and-stormy.api/routes* identity]
      (let [wrapped-handler (routes (:api sys))]
        (is (= (:metrics sys)
               (get-in (wrapped-handler {:foo :bar}) [:component :metrics])))))))
