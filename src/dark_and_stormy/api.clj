(ns dark-and-stormy.api
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY GET POST]]
            [compojure.route :as route]
            [dark-and-stormy.auth :as auth]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.geolocation :as geo]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]])
  (:import (java.util Date)))

(defn req->metrics [req auth-succeeded?]
  (let [ip (if-let [override (get-in req [:headers "x-remote-addr-override"])]
             override
             (:remote-addr req))]
    (merge {:timestamp (Date.)
            :ip ip
            :auth-result (if auth-succeeded? "pass" "fail")
            :user (get-in req [:params :username])}
           (try
             (let [{:keys [timezone country-code region] :as geo-data}
                   (geo/geolocate :ip-api ip)]
               {:local_timezone timezone
                :geo_country_code country-code
                :geo_region region
                :geo_location (select-keys geo-data [:lat :lon])})
             (catch Exception e
               (log/error e "Couldn't geolocate IP")
               {:geolocation_error_reason (.getMessage e)})))))

(defn send-auth-metric [req data]
  (metrics/send (:metrics req) "auth" data))

(defn login-handler [req]
  (let [success (apply auth/authenticate ((juxt :username :password)
                                          (:params req)))]
    (send-auth-metric req (req->metrics req success))
    (if success
      (response/redirect "/success.html")
      (response/redirect "/failure.html"))))

(defroutes routes*
  (GET "/" [] (response/resource-response "public/index.html"))
  (ANY "/login" [] (-> login-handler wrap-keyword-params wrap-params))
  (route/resources "/")
  (route/not-found "Not found."))

(defn wrap-log-exceptions [handler]
  (fn [req] (try
              (handler req)
              (catch Exception e
                (log/error e "Almost escaped...")
                (log/info req)
                (-> (response/response "Oh dear. We had a problem.")
                    (response/status 500))))))

(def routes (wrap-log-exceptions routes*))
