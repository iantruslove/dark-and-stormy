(ns dark-and-stormy.components.webserver
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [ring.adapter.jetty :as jetty]))

(defrecord JettyWebserver [config server handler]
  component/Lifecycle
  (start [this]
    (if-not (:server this)
      (let [port (config/config config :webserver :port)]
        (log/info "Starting JettyWebserver on port" port)
        (assoc this :server (jetty/run-jetty handler {:port port :join? false})))
      (do
        (log/warn "Skipping starting webserver - it's already started")
        this)))

  (stop [this]
    (if (:server this)
      (do (log/info "Stopping JettyWebserver")
          (update-in this [:server] (fn [jetty]
                                      (.stop jetty)
                                      nil)))
      (do
        (log/warn "Skipping stopping webserver - it's not running")
        this))))

(defn new
  "Return a new unstarted webserver component."
  [handler]
  (map->JettyWebserver {:handler handler}))
