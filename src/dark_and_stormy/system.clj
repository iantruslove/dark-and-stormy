(ns dark-and-stormy.system
  (:require [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.nrepl :as nrepl]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.api :as api]))

(defn start [system]
  (component/start system))

(defn stop [system]
  (component/stop system))

(defn init []
  (-> (component/system-map
       :config (config/map->Config {})
       :metrics (metrics/map->Metrics {})
       :webserver (webserver/new #' api/routes)
       :nrepl (nrepl/map->Nrepl {}))
      (component/system-using
       {:webserver [:config]
        :metrics [:config]
        :nrepl [:config]})))
