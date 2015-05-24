(ns dark-and-stormy.test.geolocation.ip-api-com
  (:require [dark-and-stormy.geolocation.ip-api-com :refer :all]
            [clojure.test :refer :all]))

(deftest the-basics
  (is (= {:lat 51.5
          :lon -0.13
          :country-code "GB"
          :region ""
          :timezone "Europe/London"}
         (geolocate* "194.72.9.34")))
  (is (= {:lat 42.5038
          :lon -83.4764
          :country-code "US"
          :region "MI"
          :timezone "America/Detroit"}
         (geolocate* "4.53.74.173"))))
