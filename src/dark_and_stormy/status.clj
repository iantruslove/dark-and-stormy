(ns dark-and-stormy.status)

(defprotocol Status
  (status [component]
    "Returns a status description for the component."))

;; Default no-op implementation
(extend-protocol Status
  java.lang.Object
  (status [this]
    ""))
