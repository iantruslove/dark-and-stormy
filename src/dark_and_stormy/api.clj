(ns dark-and-stormy.api
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes ANY GET POST]]
            [compojure.route :as route]
            [dark-and-stormy.auth :as auth]
            [ring.util.request :as request]
            [ring.util.response :as response]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]))

(defn login-handler [req]
  (try
    (apply auth/authenticate ((juxt :username :password) (:params req)))
    (response/redirect "/success.html")
    (catch Exception _
      (response/redirect "/failure.html"))))

(defroutes routes
  (GET "/" [] (response/resource-response "public/index.html"))
  (ANY "/login" [] (-> login-handler wrap-keyword-params wrap-params))
  (route/resources "/")
  (route/not-found "Not found."))
