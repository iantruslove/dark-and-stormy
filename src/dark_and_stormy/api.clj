(ns dark-and-stormy.api
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY GET POST]]
            [compojure.route :as route]
            [dark-and-stormy.auth :as auth]
            [dark-and-stormy.geolocation :as geo]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]])
  (:import (java.util Date)))

(defn req->metrics [req auth-succeeded?]
  (let [ip (:remote-addr req)
        {:keys [timezone country-code region] :as geo-data} (geo/geolocate :ip-api ip)]
    {:timestamp (Date.)
     :local_timezone timezone
     :ip ip
     :geo_country_code country-code
     :geo_region region
     :geo_location (select-keys geo-data [:lat :lon])
     :auth-result (if auth-succeeded? "pass" "fail")
     :user (get-in req [:params :username])}))

(defn login-handler [req]
  (if (apply auth/authenticate ((juxt :username :password) (:params req)))
    (response/redirect "/success.html")
    (response/redirect "/failure.html")))

(defroutes routes
  (GET "/" [] (response/resource-response "public/index.html"))
  (ANY "/login" [] (-> login-handler wrap-keyword-params wrap-params))
  (route/resources "/")
  (route/not-found "Not found."))
