(ns dark-and-stormy.api
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.util.response :as response]))

(defroutes routes
  (GET "/" [] (response/resource-response "public/index.html"))
  (route/resources "/")
  (route/not-found "Not found."))
