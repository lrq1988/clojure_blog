(ns myapp.models.schema
  (:require [clojure.java.jdbc :as jdbc]
            [noir.io :as io]
            [taoensso.timbre :as timbre]))

(def db-host "localhost")
(def db-port 3306)
(def db-name "felixblog")

(def db-spec {:classname "com.mysql.jdbc.Driver"
              :subprotocol "mysql"
              :subname (str "//" db-host ":" db-port "/" db-name "?characterEncoding=utf8")
              :user "lrq"
              :password "beijing2008"})

(defn initialized?
  "checks to see if the database initialized"
  []
  (.exists (new java.io.File (str (io/resource-path) db-name "mysql.db"))))

(defn create-blog-table []
  (jdbc/with-connection
    db-spec
    (jdbc/create-table
     :blog
     [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
     [:title "varchar(100)"]
     [:tag "varchar(100)"]
     [:summary "varchar(256)"]
     [:content "text"]
     [:postdate "varchar(20)"]
     [:pageview "INTEGER"]
     [:vote "INTEGER"]
     [:share "INTEGER"]
     [:uuid "varchar(36)"]
     )
    (jdbc/do-commands
     "CREATE INDEX postdate_index ON blog (postdate)"
     "CREATE INDEX vote_index ON blog (vote)"
     "CREATE INDEX uuid_index ON blog (uuid)")
    ))

(defn create-tables
  "create all tables of the application"
  []
  (do 
    (if (create-blog-table) (timbre/info "create blog table succeed!"))
    ))

