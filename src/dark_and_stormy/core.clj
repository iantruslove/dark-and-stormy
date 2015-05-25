(ns dark-and-stormy.core
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.system :as system])
  (:gen-class))

(defonce system (atom {}))

(defn init!
  "Initializes an unstarted system."
  []
  (reset! system (system/init)))

(defn start!
  "Starts the system."
  []
  (log/info "----> Got a deckchair? Here we go! <----")
  (swap! system system/start))

(defn stop!
  "Stops the system."
  []
  (swap! system system/stop)
  (log/info "----> Fin. <----"))

(defn -main [& args]
  (init!)
  (start!)
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop!)))
  (println "Go!"))
