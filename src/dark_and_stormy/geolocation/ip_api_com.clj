(ns dark-and-stormy.geolocation.ip-api-com
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [dark-and-stormy.util.rate-limit :refer [rate-limit]]))

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

(def geolocate (rate-limit geolocate*
                           249 #_ "max requests"
                           60000 #_ "per minute for the REST API"))
