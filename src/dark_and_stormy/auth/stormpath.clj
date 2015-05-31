(ns dark-and-stormy.auth.stormpath
  (:require [clojure.tools.logging :as log]
            [clojure.walk :as walk])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKeys)
           (com.stormpath.sdk.application Applications)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.resource ResourceException)))

(defn make-client []
  (let [api-key-builder (.build (ApiKeys/builder))]
    (.build (.setApiKey (Clients/builder) api-key-builder))))

(defn ^com.stormpath.sdk.application.Application get-application
  [client application-name]
  (let [tenant (.getCurrentTenant ^com.stormpath.sdk.client.Client client)]
    (-> tenant
        (.getApplications (Applications/where
                           (.eqIgnoreCase (Applications/name)
                                          application-name)))

        .iterator
        iterator-seq
        first)))

(defn auth-request [user pass]
  (UsernamePasswordRequest. ^java.lang.String user
                            ^java.lang.String pass))

;; TODO: It would be simpler (but not necessarily easier) to use the
;; REST API and have this return the URI instead of the Account
;; object.
(defn authenticate
  "Authenticates the user/pass pair and returns an Account if successful."
  [client user pass]
  (log/debug "Auth attempt with Stormpath for" user)
  (try
    (.getAccount (.authenticateAccount (get-application client "dark and stormy")
                                       (auth-request user pass)))
    (catch ResourceException e
      (log/info "Auth failed." {:code (.getCode e)
                                :developer-message (.getDeveloperMessage e)
                                :status (.getStatus e)}))))

(defn custom-data-href [client account]
  (let [href (.getHref (.getCustomData ^com.stormpath.sdk.resource.Extendable account))]
    (log/debug "URI for" client "is" href)
    href))

(def custom-data-metadata-keys ["href" "modifiedAt" "createdAt"])

(defn ^com.stormpath.sdk.directory.CustomData
  get-custom-data*
  "Returns the CustomData for the given account."
  [client account]
  (log/debug "Getting custom data for" account)
  (.getResource ^com.stormpath.sdk.client.Client client
                (custom-data-href client account)
                com.stormpath.sdk.directory.CustomData))

(defn get-custom-data
  "Returns a map of the account's custom data fields.
  Does not return metadata fields."
  [client account]
  (walk/keywordize-keys
   (apply dissoc
          ;; TODO: recursively postwalk the j.u.Maps to turn them into
          ;; clojure maps. Right now, only the top level map is
          ;; converted.
          (into {} (get-custom-data* client account))
          custom-data-metadata-keys)))

(defn set-custom-data [client account data]
  (-> ^com.stormpath.sdk.resource.Extendable account
      .getCustomData .delete)
  (doto (get-custom-data* client account)
    (.putAll (into {} (walk/stringify-keys data)))
    .save))

(defn empty-custom-data [client account]
  (set-custom-data client account {}))
