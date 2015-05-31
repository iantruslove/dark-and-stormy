(ns dark-and-stormy.test.api
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.api :refer :all]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.components.metrics :as metrics]
            [dark-and-stormy.components.webserver :as webserver]
            [dark-and-stormy.geo :as geo]
            [dark-and-stormy.test.helpers :refer [with-system]])
  (:import (java.util Date)))

(deftest test-extracting-metrics-data-from-req
  (let [req {:protocol "HTTP/1.1"
             :remote-addr "4.53.74.173"
             :params {:username "guest"
                      :password "nosuchpassword"}
             :route-params {}
             :headers {"origin" "http://localhost:8080"
                       "host" "localhost:8080"
                       "content-type" "application/x-www-form-urlencoded"
                       "content-length" "31"
                       "referer" "http://localhost:8080/"
                       "connection" "keep-alive"
                       "accept" "text/html"
                       "accept-language" "en-US"
                       "accept-encoding" "gzip"
                       "dnt" "1"
                       "cache-control" "max-age=0"}
             :server-port 8080
             :content-length 31
             :form-params {"username" "guest"
                           "password" "nosuchpassword"}
             :query-params {}
             :content-type "application/x-www-form-urlencoded"
             :character-encoding nil
             :uri "/login"
             :server-name "localhost"
             :query-string nil
             :body "streamy thing"
             :scheme :http
             :request-method :post}]
    (is (= {:ip "4.53.74.173"
            :user "guest"}
           (dissoc (get-basic-data req) :timestamp)))
    (let [geo-data (-> req get-basic-data add-geolocation-data)]
      (is (= {:geo_country_code "US"
              :geo_region "MI"
              :local_timezone "America/Detroit"}
             (dissoc geo-data :geo_location :timestamp :ip :user)))
      (let [loc (:geo_location geo-data)]
        (is (< 42.5 (:lat loc) 42.6))
        (is (< -83.5 (:lon loc) -83.4))))))
