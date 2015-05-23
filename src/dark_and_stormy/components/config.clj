(ns dark-and-stormy.components.config
  (:require [carica.core :as carica]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]))

(defrecord Config []
  component/Lifecycle
  (start [this]
    (log/info "Starting Config")
    ;; Reset all of Carica's state
    (assoc this :config (carica/configurer (io/resource "config.json"))))

  (stop [this]
    (log/info "Stopping Config")
    this))

(defn config [this & keys]
  (apply (:config this) keys))
