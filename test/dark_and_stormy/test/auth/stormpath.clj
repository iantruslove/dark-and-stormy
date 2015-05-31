(ns dark-and-stormy.test.auth.stormpath
  (:require [clojure.test :refer :all]
            [dark-and-stormy.auth.stormpath :refer :all]))

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
                                      :map {:a 1 :b 2}
                                      :array [1 2 3 4]})
    (is (= {:foo "bar" :map {"a" 1 "b" 2} :array [1 2 3 4]}
           (get-custom-data @client account)))))
