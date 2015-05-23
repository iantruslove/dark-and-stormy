(ns dark-and-stormy.components.nrepl
  (:require [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]))

(defn nrepl-opts [nrepl-config]
  (let [{:keys [port] insert-cider-middleware :cider_middleware} nrepl-config]
    (cond-> [:port port]
      insert-cider-middleware ((fn [opts] (log/info "Enabling Cider middleware")
                                 (into opts [:handler cider-nrepl-handler]))))))

(defrecord Nrepl [config]
  component/Lifecycle
  (start [this]
    (if-not (:nrepl this)
      (let [nrepl-config (config/config config :nrepl)]
        (log/info "Starting nREPL on port" (:port nrepl-config))
        (assoc this :nrepl (apply nrepl/start-server (nrepl-opts nrepl-config))))
      (do
        (log/warn "Skipping starting nREPL - it's already started")
        this)))

  (stop [this]
    (if (:nrepl this)
      (do (log/info "Stopping nREPL")
          (update-in this [:nrepl] (fn [nrepl] (nrepl/stop-server nrepl)
                                     nil)))
      (do
        (log/warn "Skipping stopping nREPL - it's not running")
        this))))
