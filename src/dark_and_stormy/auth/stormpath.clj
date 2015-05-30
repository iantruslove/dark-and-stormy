(ns dark-and-stormy.auth.stormpath
  (:require [clojure.tools.logging :as log])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKeys)
           (com.stormpath.sdk.application Applications)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.resource ResourceException)))

(defn ^com.stormpath.sdk.application.Application get-application
  [^com.stormpath.sdk.client.Client client application-name]
  (let [tenant (.getCurrentTenant ^com.stormpath.sdk.client.Client client)]
    (-> tenant
        (.getApplications (Applications/where
                           (.eqIgnoreCase (Applications/name)
                                          application-name)))

        .iterator
        iterator-seq
        first)))

(defn make-client []
  (let [api-key-builder (.build (ApiKeys/builder))]
    (.build (.setApiKey (Clients/builder) api-key-builder))))

(defn authenticate [^com.stormpath.sdk.client.Client client
                    ^java.lang.String user
                    ^java.lang.String pass]
  (let [auth-request (UsernamePasswordRequest. user pass)]
    (log/debug "Auth attempt with Stormpath for" user)
    (try
      (.authenticateAccount (get-application client "dark and stormy")
                            auth-request)
      (catch ResourceException e
        (log/info "Auth failed." {:code (.getCode e)
                                  :developer-message (.getDeveloperMessage e)
                                  :status (.getStatus e)})))))
