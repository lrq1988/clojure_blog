(defproject myapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [lib-noir "0.7.1"]
                 [compojure "1.1.5"]
                 [ring-server "0.3.0"]
                 [selmer "0.4.8"]
                 [com.taoensso/timbre "2.6.3"]
                 [com.postspectacular/rotor "0.1.0"]
                 [com.taoensso/tower "1.7.1"]
                 [markdown-clj "0.9.33"]
                 [pinyin4clj "0.2.0"]
                 [korma "0.3.0-RC5"]
		 [org.clojure/java.jdbc "0.3.0-alpha5"]
		 [mysql/mysql-connector-java "5.1.25"]
                 [clj-soup/clojure-soup "0.1.0"]
		 [uk.org.alienscience/leiningen-war "0.0.12"]]
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler myapp.handler/app
         :init    myapp.handler/init
         :destroy myapp.handler/destroy}
  :profiles
  {:production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.2.0"]]}}
  :min-lein-version "2.0.0")
