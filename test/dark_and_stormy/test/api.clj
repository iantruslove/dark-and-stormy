(ns dark-and-stormy.test.api
  (:require [dark-and-stormy.api :refer :all]
            [dark-and-stormy.geolocation :as geo]
            [clojure.test :refer :all]))

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
             :request-method :post}
        metrics (req->metrics req false)]
    (are [k expected-val] (= expected-val (metrics k))
         :local_timezone "America/Detroit"
         :ip "4.53.74.173"
         :geo_country_code "US"
         :geo_region "MI"
         :auth-result "fail"
         :user "guest")
    (let [loc (:geo_location metrics)]
      (is (< 42.5 (:lat loc) 42.6))
      (is (< -83.5 (:lon loc) -83.4)))))
