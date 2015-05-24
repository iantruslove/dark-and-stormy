(ns dark-and-stormy.geolocation.flaky
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.util.stats :as stats]))

(def sample-cities
  {:denver [39.99 -113.2]
   :new-york [38.7 -82.1]
   :san-francisco [37.3 -129.0]
   :chicago [45.4 -99.1]
   :san-mateo [37.2 -129.1]
   :london [52.0 0.0]})

(def geolocation-lookup-failure-probability 0.2)

(def geolocation-avg-latency 2000 #_ msecs)

(def latency (partial stats/sample geolocation-avg-latency))

;; This implementation happens to be entirely fake, and is highly
;; likely to fail and throw an exception.
(defn geolocate [ip]
  (Thread/sleep (latency))
  (if (> (rand) geolocation-lookup-failure-probability)
    (do
      (log/info "Geolocation succeeded")
      (-> (rand-nth (keys sample-cities))
          sample-cities))
    (do
      (log/info "Geolocation failed")
      (throw (Exception. (str "Encountered some kind of network timeout problem"
                              " geolocating an IP."))))))
