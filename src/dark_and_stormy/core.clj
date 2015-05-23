(ns dark-and-stormy.core
  (:require [clojure.tools.logging :as log]
            [dark-and-stormy.system :as system]))

(defonce system (atom {}))

(defn start! []
  (log/info "----> Got a deckchair? Here we go! <----")
  (swap! system system/start))

(defn stop! []
  (swap! system system/stop)
  (log/info "----> Fin. <----"))

(defn -main [& args]
  (reset! system (system/init))
  (start!))
