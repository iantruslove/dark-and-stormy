(ns dark-and-stormy.components.api
  (:require [com.stuartsierra.component :as component]
            [dark-and-stormy.api :as api]
            [dark-and-stormy.status :as status]))

(defn wrap-component [handler component]
  (fn [req]
    (handler (assoc req :component component))))

(defn routes [this]
  (-> #'api/routes*
      (wrap-component this)
      api/wrap-log-exceptions))

(defrecord Api [metrics]
  component/Lifecycle
  (start [this]
    (assert (:metrics this))
    this)

  (stop [this]
    this)

  status/Status
  (status [this]
    "ok"))
