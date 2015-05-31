(ns dark-and-stormy.components.auth
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [dark-and-stormy.status :as status]
            [dark-and-stormy.auth.stormpath :as stormpath]
            [dark-and-stormy.util.stats :as stats]))

(defprotocol AuthService
  (authenticate [this auth-data]
    "Returns truthy if the authc attempt succeeds, falsy otherwise."))

(defn async-record-auth-result
  [authenticated-user this auth-request-data]
  (future
    (log/debug "auth-request-data:" auth-request-data)
    (stormpath/record-auth-result (:client this)
                                  authenticated-user
                                  auth-request-data)))

(defrecord StormpathAuth [client]
  AuthService
  (authenticate [this {:keys [user pass] :as auth-data}]
    (log/debug "Auth data:" auth-data)
    (doto (stormpath/authenticate (:client this) user pass)
      (async-record-auth-result this (dissoc auth-data :pass))))

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
