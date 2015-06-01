(ns dark-and-stormy.auth.stormpath
  (:require [clojure.tools.logging :as log]
            [clojure.walk :as walk]
            [dark-and-stormy.geo :as geo])
  (:import (com.stormpath.sdk.client Client Clients)
           (com.stormpath.sdk.api ApiKeys)
           (com.stormpath.sdk.application Applications)
           (com.stormpath.sdk.authc UsernamePasswordRequest)
           (com.stormpath.sdk.resource ResourceException)
           (org.joda.time DateTime)))

;; TODO: This should be configured or injected
(def application-name "dark and stormy")

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

(defn resource-exception-data [^ResourceException e]
  {:code (.getCode e)
   :developer-message (.getDeveloperMessage e)
   :status (.getStatus e)})

;; TODO: It would be simpler (but not necessarily easier) to use the
;; REST API and have this return the URI instead of the Account
;; object.
(defn authenticate
  "Authenticates the user/pass pair and returns an Account if successful."
  [client user pass]
  (log/debug "Auth attempt with Stormpath for" user)
  (try
    (.getAccount (.authenticateAccount (get-application client application-name)
                                       (auth-request user pass)))
    (catch ResourceException e
      (log/info "Auth failed." (resource-exception-data e)))))

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

;; TODO: I think it might be nicer to deal with data fields more
;; discretely - so instead of (get-custom-data c a), do
;; (get-custom-data c a :some-key).
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
  (locking set-custom-data ;; Ugh... well, it's atomic now...
    (try (-> ^com.stormpath.sdk.resource.Extendable account
             .getCustomData .delete)
         (catch ResourceException e
           (log/warn "Error deleting custom data." (resource-exception-data e))))
    (doto (get-custom-data* client account)
      (.putAll (walk/stringify-keys data))
      .save)))

(defn empty-custom-data [client account]
  (set-custom-data client account {}))

(defn add-location-entry
  "Appends a new location to custom-data's :location.
  Keeps only the most recent 100 locations."
  [location custom-data]
  (let [updated-data (update-in custom-data [:locations]
                                (fn [locations]
                                  (conj (->> locations
                                             (into [])
                                             ;; TODO: sort by timestamp here
                                             (take-last 99)
                                             (into []))
                                        location)))]
    updated-data))

(defn application-groups
  "Returns the set of all groups in this application."
  [client application-name]
  (let [application (get-application client application-name)]
    (-> application
        .getGroups
        .iterator
        iterator-seq
        set)))

(defn account-group-memberships [account]
  "Returns a set of all the group memberships for an account."
  (->> ^com.stormpath.sdk.account.Account account
       .getGroupMemberships
       .iterator
       iterator-seq
       set))

(defn group-membership-group-name
  "Returns the group name from a GroupMembership"
  [^com.stormpath.sdk.group.GroupMembership membership]
  (-> membership
      .getGroup
      .getName))

(defn in-group? [account group-name]
  (->> (account-group-memberships account)
       (map group-membership-group-name)
       (some #(= group-name %))))

(defn add-to-group [client account group-name]
  (if-let [group
           ^com.stormpath.sdk.group.Group
           (some (fn [^com.stormpath.sdk.group.Group group]
                   (when (= group-name (.getName group))
                     group))
                 (application-groups client application-name))]
    (if (in-group? account (.getName group))
      (log/info "No need to add membership to group" (.getName group))
      (.addGroup ^com.stormpath.sdk.account.Account account group))
    (log/error "Group doesn't exist:" group-name)))

(defn remove-from-group [account group-name]
  (when-let [membership
             (->> (account-group-memberships account)
                  (some (fn [membership]
                          (when (= group-name (group-membership-group-name membership))
                            membership))))]
    (.delete ^com.stormpath.sdk.group.GroupMembership membership)))

(defn velocities
  "Returns the average speeds required for the last four logins."
  [locations]
  (->> locations
       (into [])
       (take-last 4)
       (map (partial into {}))
       (map walk/keywordize-keys)
       (partition 2 1)
       (map (partial apply geo/velocity))))

(defn velocity-check [client account locations]
  (let [velocities (velocities locations)
        v-max (apply max velocities)]
    (if (> v-max 312 #_ "312m/s = 700mph")
      (do (log/info "Account triggered velocity alarm. Velocity" v-max "m/s of" velocities)
          (add-to-group client account "suspicious_velocity"))
      (log/debug "Velocity ok: max " v-max "of" velocities))))

(defn track-velocity
  "Stores a map of [:lat :lon :timestamp] in the account's :location
  custom data field."
  [client authenticated-user {:keys [geo_location timestamp] :as auth-request-data}]
  ;; TODO: authc fails are interesting too. Look up the account a different way.
  (when-let [account authenticated-user]
    (log/debug "Updating user custom data:" auth-request-data)
    ;; TODO: make this threading form threadsafe. It's not. That's a problem.
    (let [updated-data
          (->> (get-custom-data client account)
               (add-location-entry (assoc geo_location
                                     :timestamp (str (DateTime. timestamp)))))]
      (set-custom-data client account updated-data)
      (velocity-check client account (:locations updated-data)))))
