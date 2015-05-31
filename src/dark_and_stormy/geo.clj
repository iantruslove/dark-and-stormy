(ns dark-and-stormy.geo
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [dark-and-stormy.util.rate-limit :refer [rate-limit]]
            [geo.spatial :as spatial])
  (:import (org.joda.time DateTime)))

(defn velocity
  "Returns the average velocity in m/s to travel between the two points.
  Args must be maps of [:lat :lon :timestamp]."
  [from to]
  (let [metres (spatial/distance
                (apply spatial/spatial4j-point ((juxt :lat :lon) from))
                (apply spatial/spatial4j-point ((juxt :lat :lon) to)))
        seconds (/ (- (.getTime (.toDate (DateTime. (:timestamp to))))
                      (.getTime (.toDate (DateTime. (:timestamp from)))))
                   1000)]
    (/ metres seconds)))

(defn geolocate*
  "Uses ip-api.com to geolocate an IP address. Returns a [lat lon] pair."
  [ip]
  (let [{:keys [timezone region lat lon countryCode]}
        (doto (-> (http/get (str "http://ip-api.com/json/" ip) {:as :json})
                  :body)
          log/debug)]
    {:lat lat
     :lon lon
     :country-code countryCode
     :region region
     :timezone timezone}))

;; TODO: perhaps the return here could be a record, or it could have a
;; validated shape using Prismatic Schema.
(def ^{:arglists '([ip])}
  geolocate
  "Returns a map of geo data for the given IP address. Includes:
  :lat
  :lon
  :country-code - ISO 3166 country code e.g. \"US\" - http://geotags.com/iso3166/countries.html
  :region - e.g. \"CO\"
  :timezone - e.g. \"America/Denver\""
  (rate-limit geolocate*
              249 #_ "max requests"
              60000 #_ "per minute for the REST API"))
