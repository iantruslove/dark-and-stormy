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
    (assoc this :config (carica/configurer (io/resource "config.edn"))))

  (stop [this]
    (log/info "Stopping Config")
    this)

  status/Status
  (status [this] "ok"))

(defn config [this & keys]
  (apply (:config this) keys))
