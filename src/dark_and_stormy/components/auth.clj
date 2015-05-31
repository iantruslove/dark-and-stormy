(ns dark-and-stormy.components.auth
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [dark-and-stormy.status :as status]
            [dark-and-stormy.auth.stormpath :as stormpath]
            [dark-and-stormy.util.stats :as stats]))

(defprotocol AuthService
  (authenticate [this user pass]))

(defrecord StormpathAuth [client]
  AuthService
  (authenticate [this user pass]
    (stormpath/authenticate (:client this) user pass))

  component/Lifecycle
  (start [this]
    (assoc this :client (stormpath/make-client)))

  (stop [this]
    (dissoc this :client))

  status/Status
  (status [this]
    (if (:client this)
      "Client initialized"
      "stopped")))
