(ns dark-and-stormy.geolocation
  (:require [clj-http.client :as http]
            [dark-and-stormy.geolocation.flaky :as flaky]
            [dark-and-stormy.geolocation.ip-api-com :as ip-api]))

(defmulti geolocate
  "Returns a [lat lon] pair for the given IP address."
  {:arglists '([provider ip])}
  (fn [provider ip] provider))

(defmethod geolocate :flaky
  [_ ip]
  (flaky/geolocate ip))

(defmethod geolocate :ip-api
  [_ ip]
  (ip-api/geolocate ip))
