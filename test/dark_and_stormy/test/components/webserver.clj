(ns dark-and-stormy.test.components.webserver
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.api :as api]
            [dark-and-stormy.components.auth :as auth]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.test.helpers :refer [with-system]])
  (:import (java.util Date)))

(defrecord FakeMetrics [config]
  ;; No need to implement component/Lifecycle, since Object already
  ;; has a stub implementation.
  )

(deftest test-running-webserver
  (with-system [sys (-> (component/system-map
                         :api (api/map->Api {})
                         :auth (auth/map->DodgyAuth {})
                         :config (config/map->Config {})
                         :metrics (map->FakeMetrics {})
                         :webserver (webserver/map->JettyWebserver {}))
                        (component/system-using
                         {:api [:auth :metrics]
                          :metrics [:config]
                          :webserver [:api :config :metrics]}))]
    (is (= 200 (-> (str "http://localhost:"
                        (config/config (:config sys) :webserver :port))
                   http/get
                   :status)))))
