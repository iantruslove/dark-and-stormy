(ns dark-and-stormy.components.nrepl
  (:require [cider.nrepl :refer [cider-nrepl-handler]]
            [clojure.tools.logging :as log]
            [clojure.tools.nrepl.server :as nrepl]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.status :as status]))

(defn nrepl-opts [nrepl-config]
  (let [{:keys [port] insert-cider-middleware :cider_middleware} nrepl-config]
    (cond-> [:port port]
      insert-cider-middleware ((fn [opts] (log/info "Enabling Cider middleware")
                                 (into opts [:handler cider-nrepl-handler]))))))

(defrecord Nrepl [config server]
  component/Lifecycle
  (start [this]
    (if-not (:server this)
      (let [nrepl-config (config/config config :nrepl)]
        (log/info "Starting nREPL on port" (:port nrepl-config))
        (assoc this :server (apply nrepl/start-server (nrepl-opts nrepl-config))))
      (do
        (log/warn "Skipping starting nREPL - it's already started")
        this)))

  (stop [this]
    (if (:server this)
      (do (log/info "Stopping nREPL")
          (update-in this [:server] (fn [nrepl] (nrepl/stop-server nrepl)
                                      nil)))
      (do
        (log/warn "Skipping stopping nREPL - it's not running")
        this)))

  status/Status
  (status [this]
    (if (:server this)
      (str "STARTED. Listening on port " (config/config (:config this) :nrepl :port))
      "STOPPED.")))
