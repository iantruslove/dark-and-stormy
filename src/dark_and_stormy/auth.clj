(ns dark-and-stormy.auth
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.util :as util]))

(def authc-failure-probability 0.3)

(def authc-avg-latency 200 #_ msecs)

(def latency (partial util/sample authc-avg-latency))

(defn authenticate
  "Returns truthy to indicate a successful auth attempt, and throws exceptions
  if authc fails.

  Right now, guest/guest is the only allowed user/pass combo, and the
  function will fail to authenticate every so often just
  because... :)"
  [username password]
  (Thread/sleep (latency))
  (if (and (> (rand) authc-failure-probability)
           (= [username password] ["guest" "guest"]))
    (do
      (log/info "Auth succeeded")
      true)
    (do
      (log/info "Auth failed")
      (throw (Exception. "Authentication failed.")))))
