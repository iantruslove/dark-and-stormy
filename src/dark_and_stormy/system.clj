(ns dark-and-stormy.system
  (:require [com.stuartsierra.component :as component]
            [dark-and-stormy.api :as api]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.nrepl :as nrepl]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.status :as status]
            [doric.core :as doric]))

(defn start [system]
  (component/start system))

(defn stop [system]
  (component/stop system))

(defn status [system]
  (println "System status:")
  (println (doric/table [:component :status]
                        (for [[component-name component] system]
                          {:component (name component-name)
                           :status (status/status component)}))))

(defn init []
  (-> (component/system-map
       :config (config/map->Config {})
       :metrics (metrics/map->Metrics {})
       :webserver (webserver/new #'api/routes)
       :nrepl (nrepl/map->Nrepl {}))
      (component/system-using
       {:webserver [:config :metrics]
        :metrics [:config]
        :nrepl [:config]})))
