(ns dark-and-stormy.test.auth.stormpath
  (:require [clojure.test :refer :all]
            [dark-and-stormy.auth.stormpath :refer :all])
  (:import (java.util Date)))

(defonce client (atom nil))

(defn client-fixture [f]
  (when-not @client
    (reset! client (make-client)))
  (f))

(use-fixtures :once client-fixture)

(deftest test-stormpath-auth
  (testing "authc pass/fail"
    (is (not (authenticate @client "nosuchuser" "badpass")))
    (is (authenticate @client "guest" "guest")))
  (testing "authentication returns the account URI"
    (is (instance? com.stormpath.sdk.account.Account
                   (authenticate @client "guest" "guest")))))

(deftest test-custom-data
  (let [account (authenticate @client "testuser" "testuser")]
    (is account "Guard assert")
    (empty-custom-data @client account)
    (is (= {} (get-custom-data @client account)))
    (set-custom-data @client account {:foo "bar"
                                      :num 1.234
                                      :time (Date. 1433100184374)
                                      :map {:a 1 :b 2}
                                      :array [1 2 3 4]})
    (is (= {:foo "bar"
            :num 1.234M
            :time 1433100184374
            :map {"a" 1 "b" 2}
            :array [1 2 3 4]}
           (get-custom-data @client account)))))

(deftest test-add-location-data
  (is (= {:locations [{:lat 1 :lon 2 :timestamp :t}]}
         (add-location-entry {:lat 1 :lon 2 :timestamp :t} {})))
  (is (= {:locations [{:lat 1 :lon 2 :timestamp :t}]}
         (add-location-entry {:lat 1 :lon 2 :timestamp :t} {:locations []})))
  (is (= {:locations [{:lat 0 :lon 0 :timestamp 0}
                      {:lat 1 :lon 2 :timestamp :t}]}
         (add-location-entry {:lat 1 :lon 2 :timestamp :t}
                             {:locations [{:lat 0 :lon 0 :timestamp 0}]})))
  (let [updated-data (add-location-entry
                      {:lat 1 :lon 2 :timestamp :t}
                      {:locations (into [] (repeat 200 {:lat 0 :lon 0 :timestamp 0}))})]
    (is (= 100 (-> updated-data
                   :locations
                   count)))
    (is (= {:lat 1 :lon 2 :timestamp :t}
           (->> updated-data
                :locations
                (take-last 1)
                first)))))

(deftest test-velocities
  (is (every? (fn [v] (< 410 v 440))
              (velocities [{:lat 0 :lon 0 :timestamp "2015-05-10T12:00:00.000Z"}
                           {:lat 10 :lon 10 :timestamp "2015-05-10T13:00:00.000Z"}
                           {:lat 20 :lon 20 :timestamp "2015-05-10T14:00:00.000Z"}
                           {:lat 30 :lon 30 :timestamp "2015-05-10T15:00:00.000Z"}]))))

(deftest test-group-membership
  (let [group-name "test_group"
        account (authenticate @client "testuser" "testuser")]
    (remove-from-group account group-name)
    (is (not (in-group? account group-name)))
    (add-to-group @client account group-name)
    (is (in-group? account group-name))
    (remove-from-group account group-name)
    (is (not (in-group? account group-name)))))
