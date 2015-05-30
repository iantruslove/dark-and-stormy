(ns dark-and-stormy.auth.dodgy
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.util.stats :as stats]))

(def authc-failure-probability 0.3)

(def authc-avg-latency 200 #_ msecs)

(def latency (partial stats/sample authc-avg-latency))

(defn authenticate
  "Returns truthy to indicate a successful auth attempt, false otherwise.

  Right now, guest/guest is the only allowed user/pass combo, and the
  function will fail to authenticate every so often just
  because... :)"
  [username password]
  (Thread/sleep (latency))
  (if (and (> (rand) authc-failure-probability)
           (= [username password] ["guest" "guest"]))
    (doto "Auth succeeded" log/info)
    (log/info "Auth failed")))
