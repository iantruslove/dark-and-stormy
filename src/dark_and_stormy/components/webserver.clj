(ns dark-and-stormy.components.webserver
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.api :as api]
            [dark-and-stormy.status :as status]
            [ring.adapter.jetty :as jetty]))

(defrecord JettyWebserver [api config metrics server]
  component/Lifecycle
  (start [this]
    (if-not (:server this)
      (let [port (config/config config :webserver :port)]
        (log/info "Starting JettyWebserver on port" port)
        (assoc this :server
               (jetty/run-jetty (api/routes (:api this))
                                {:port port :join? false})))
      (do
        (log/warn "Skipping starting webserver - it's already started")
        this)))

  (stop [this]
    (if (:server this)
      (do (log/info "Stopping JettyWebserver")
          (update-in this [:server]
                     (fn [^org.eclipse.jetty.util.component.LifeCycle jetty]
                       (.stop jetty)
                       nil)))
      (do
        (log/warn "Skipping stopping webserver - it's not running")
        this)))

  status/Status
  (status [this]
    (if (:server this)
      (str "STARTED. Listening on port "
           (config/config (:config this) :webserver :port))
      "STOPPED.")))
