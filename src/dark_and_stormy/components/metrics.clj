(ns dark-and-stormy.components.metrics
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [dark-and-stormy.components.config :as config]
            [dark-and-stormy.status :as status])
  (:refer-clojure :exclude [send]))

(defn make-url
  "Helper to construct ES URLs, getting the host and port from the
  running metrics component's deps."
  [this & path-parts]
  (str
   (:url this)
   (when path-parts
     (apply str (when-not (= \/ (ffirst path-parts)) "/")
            path-parts))))

(defn template-exists? [this template-name]
  (let [get-status (:status (http/get (make-url this "_template/" template-name)
                                      {:throw-exceptions false}))]
    (log/debug "Does" template-name "exist? Get status:" get-status)
    (= 200 get-status)))

(defn install-template [this template-name template-data]
  (log/info "Installing template" template-name)
  (http/put (make-url this "_template/" template-name)
            {:body (json/generate-string template-data)}))

(defn get-configured-templates [this]
  (config/config (:config this) :metrics :templates))

(defn ensure-all-templates
  "Idempotently installs all templates from the config.

  ES templates set up pattern-based index creation, which in turn set
  up mapping creation. The templates configured in the config should
  be enough to bootstrap a brand-new ES instance."
  [this]
  (let [templates (get-configured-templates this)]
    (doseq [[template template-data] templates
            :let [template-name (name template)]]
      (if-not (template-exists? this template-name)
        (install-template this template-name template-data)
        (log/info "Skipping already-installed template:" template-name)))))

(defn current-index
  "Placeholder for date-based index rolling."
  [this]
  (config/config (:config this) :metrics :index))

(defprotocol MetricsService
  (send [this metric-type data]))

(defn send*
  "Sends a data payload to the metrics datastore.
  `data` should be a map."
  [es-url metric-type data]
  (log/debug data)
  (try
    (http/post es-url {:body (json/generate-string data)})
    (catch Throwable t
      (log/error t "Error sending metric to Metrics service."))))

(defrecord Metrics [config url]
  MetricsService
  (send [this metric-type data]
    (if (re-find #"^\d+\.\d+.\d+\.\d+$" (:ip data))
      (send* (make-url this (current-index this) "/" metric-type) metric-type data)
      (log/warn "Invalid IP address:" (:ip data))))

  component/Lifecycle
  (start [this]
    (if-not (:url this)
      (if-let [url (config/config config :metrics :url)]
        (do
          (log/info "Starting metrics service using" url)
          (doto (assoc this :url url) ;; :url is used as the "running" flag
            ensure-all-templates))
        (throw (Exception. "Missing config url.")))
      (do
        (log/warn "Skipping starting metrics service - it's already started")
        this)))

  (stop [this]
    (if (:url this)
      (do
        (log/info "Stopping metrics service")
        (dissoc this :url))
      (do
        (log/warn "Skipping stopping metrics service - it's not running")
        this)))

  status/Status
  (status [this]
    (if (:url this)
      (str "STARTED. Using ES at " (:url this))
      "STOPPED.")))
