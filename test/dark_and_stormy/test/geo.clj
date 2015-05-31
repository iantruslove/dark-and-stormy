(ns dark-and-stormy.test.geo
  (:require [dark-and-stormy.geo :refer :all]
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

(deftest test-velocity
  ;; It's 7554 km from Denver to London, according to Wolfram Alpha.
  ;; To travel 7500km in 8 hours you'd need to travel just under 1000km/h.
  ;; That's ~260m/s.
  (let [london {:lat 51.5 :lon 0.1 :timestamp "2015-05-10T12:00:00.000Z"}
        denver {:lat 39.7 :lon -104.9 :timestamp "2015-05-10T20:00:00.000Z"}]
    (is (< 260
           (velocity london denver)
           270))))
