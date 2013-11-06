(ns myapp.handler  
  (:require [compojure.core :refer [defroutes]]            
            [myapp.routes.home :refer [home-routes]]
            [noir.util.middleware :as middleware]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]))

(defroutes app-routes
  (route/resources "/")
  (route/not-found "Not Found!"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/append})
  
  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "myapp.log" :max-size (* 512 1024) :backlog 10})
  
  (timbre/info "myapp started successfully"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "myapp is shutting down..."))

(def app (middleware/app-handler
           ;; add your application routes here
           [home-routes app-routes]
           ;; add custom middleware here
           :middleware []
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn]))
