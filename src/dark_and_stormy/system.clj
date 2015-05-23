(ns dark-and-stormy.system
  (:require [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.nrepl :as nrepl]
            [dark-and-stormy.components.webserver :as webserver]))

(defn start [system]
  (component/start system))

(defn stop [system]
  (component/stop system))

(defn init []
  (-> (component/system-map
       :config (config/map->Config {})
       :webserver (webserver/map->JettyWebserver {})
       :nrepl (nrepl/map->Nrepl {}))
      (component/system-using
       {:webserver [:config]
        :nrepl [:config]})))
