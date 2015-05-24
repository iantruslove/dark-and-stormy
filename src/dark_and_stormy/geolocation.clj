(ns dark-and-stormy.geolocation
  (:require [clj-http.client :as http]
            [dark-and-stormy.geolocation.flaky :as flaky]
            [dark-and-stormy.geolocation.ip-api-com :as ip-api]))

(defmulti geolocate
  "Returns a map of geo data for the given IP
  address. Includes:
  :lat
  :lon
  :country-code - ISO 3166 country code e.g. \"US\" - http://geotags.com/iso3166/countries.html
  :region - e.g. \"CO\"
  :timezone - e.g. \"America/Denver\""
  {:arglists '([provider ip])}
  (fn [provider ip] provider))

(defmethod geolocate :flaky
  [_ ip]
  (flaky/geolocate ip))

(defmethod geolocate :ip-api
  [_ ip]
  (ip-api/geolocate ip))
