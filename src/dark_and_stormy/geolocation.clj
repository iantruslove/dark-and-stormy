(ns dark-and-stormy.geolocation
  (:require [clj-http.client :as http]
            [dark-and-stormy.geolocation.ip-api-com :as ip-api]))

;; TODO: perhaps the return here could be a record, or it could have a
;; validated shape using Prismatic Schema.
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

(defmethod geolocate :ip-api
  [_ ip]
  (ip-api/geolocate ip))
