(ns dark-and-stormy.components.webserver
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [ring.adapter.jetty :as jetty]))

(defn handler [req]
  {:status 200 :body "Hi"})

(defrecord JettyWebserver [config]
  component/Lifecycle
  (start [this]
    (if-not (:jetty this)
      (let [port (config/config config :webserver :port)]
        (log/info "Starting JettyWebserver on port" port)
        (assoc this :jetty (jetty/run-jetty #'handler {:port port :join? false})))
      (do
        (log/warn "Skipping starting webserver - it's already started")
        this)))

  (stop [this]
    (if (:jetty this)
      (do (log/info "Stopping JettyWebserver")
          (update-in this [:jetty] (fn [jetty]
                                     (.stop jetty)
                                     nil)))
      (do
        (log/warn "Skipping stopping webserver - it's not running")
        this))))
