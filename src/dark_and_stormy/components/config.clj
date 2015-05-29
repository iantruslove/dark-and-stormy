(ns dark-and-stormy.components.config
  (:require [carica.core :as carica]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.status :as status]))

(defrecord Config []
  component/Lifecycle
  (start [this]
    (log/info "Starting Config")
    ;; Reset all of Carica's state
    (let [config (carica/configurer (io/resource "config.edn"))
          override-config (carica/overrider* config)]
      ;; Heroku sets a PORT env var. That's important to capture.
      (assoc this :config (if-let [env-port (System/getenv "PORT")]
                            (do (log/info (str "Overriding :webserver :port config to " env-port))
                                (override-config :webserver :port (Integer. env-port)))
                            config))))

  (stop [this]
    (log/info "Stopping Config")
    this)

  status/Status
  (status [this] "ok"))

(defn config [this & keys]
  (apply (:config this) keys))
