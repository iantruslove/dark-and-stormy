(ns dark-and-stormy.components.auth
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [dark-and-stormy.status :as status]
            [dark-and-stormy.auth.dodgy :as dodgy]
            [dark-and-stormy.util.stats :as stats]))

(defprotocol AuthService
  (authenticate [this user pass]))

(defrecord DodgyAuth []
  AuthService
  (authenticate [_ user pass]
    (dodgy/authenticate user pass))

  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    this)

  status/Status
  (status [this]
    "ok"))
