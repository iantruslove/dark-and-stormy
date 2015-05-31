(ns dark-and-stormy.api
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY GET POST]]
            [compojure.route :as route]
            [dark-and-stormy.components.auth :as auth]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.geolocation :as geo]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]])
  (:import (java.util Date)))

(defn get-basic-data [req]
    (let [ip (some identity
                 [(get-in req [:headers "x-remote-addr-override"])
                  (get-in req [:headers "x-forwarded-for"])
                  (:remote-addr req)])]
      {:timestamp (Date.)
       :ip ip
       :user (get-in req [:params :username])}))

(defn add-geolocation-data [data]
  (merge data
         (try
           (let [{:keys [timezone country-code region] :as geo-data}
                 (geo/geolocate (:ip data))]
             {:local_timezone timezone
              :geo_country_code country-code
              :geo_region region
              :geo_location (select-keys geo-data [:lat :lon])})
           (catch Exception e
             (log/error e "Couldn't geolocate IP")
             {:geolocation_error_reason (.getMessage e)}))))

(defn add-authenticated-data [data auth-succeeded?]
  (assoc data :auth-result (if auth-succeeded? "pass" "fail")))

;; TODO: take the metrics component instead of req
(defn send-auth-metric [req data]
  (metrics/send (get-in req [:component :metrics]) "auth" data))

(defn login-handler [req]
  (let [req-data (-> req
                     get-basic-data
                     add-geolocation-data)
        success (auth/authenticate
                 (get-in req [:component :auth])
                 (merge req-data
                        {:user (:username (:params req))
                         :pass (:password (:params req))}))]
    (send-auth-metric req (add-authenticated-data req-data success))
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
