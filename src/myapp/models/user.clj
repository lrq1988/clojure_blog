(ns myapp.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [myapp.models.schema :as schema]
            [noir.session :as session]
            [myapp.util :as util]))

(defn login [username password]
   (let [result (first
            (jdbc/with-connection
              schema/db-spec
              (jdbc/with-query-results
                rs [(str "select * from user where username=" "'" username "'")]
                (take 1 rs))))
         valid (if (nil? (:username result))
                 "nouser"
                 (if (= (:password result) password)
                   "ok" "pwdnomatch"))]
         (do (prn (str "login " valid))) (str valid "")))
         

(defn logout []
  (session/put! :username nil)
  "ok")

(defn query-user-data [username]
  (do
    (let [sql (str "select * from user where username=" "'" username "'")
          result (conj 
          (jdbc/with-connection
            schema/db-spec
            (jdbc/with-query-results rs [sql]
              (take 1 rs))) nil)]
      (prn result))))
              
