(ns dark-and-stormy.geolocation.flaky
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.util.stats :as stats]))

(def sample-cities
  {:denver {:lat 39.99 :lon -113.2 :country-code "US" :region "CO"
            :timezone "America/Denver"}
   :new-york {:lat 38.7 :lon -82.1 :country-code "US" :region "NY"
              :timezone "America/New_York"}
   :san-francisco {:lat 37.3 :lon -129.0 :country-code "US"
                   :region "CA" :timezone "America/Los_Angeles"}
   :chicago {:lat 45.4 :lon -99.1 :country-code "US" :region "IL"
             :timezone "America/Chicago"}
   :san-mateo {:lat 37.2 :lon -129.1 :country-code "US" :region "CA"
               :timezone "America/Los_Angeles"}
   :london {:lat 52.0 :lon 0.0 :country-code "GB"
            :timezone "Europe/London"}})

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
