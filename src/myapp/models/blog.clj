
(ns myapp.models.blog
  (:use pinyin4clj.core)
  (:require [myapp.util :as util]
            [clojure.java.jdbc :as jdbc]
            [noir.io :as io]
            [clojure.contrib.io]
            [myapp.models.schema :as schema])
  (:import [java.text SimpleDateFormat]
           [java.util Date]
           [java.io File InputStreamReader FileInputStream BufferedReader OutputStreamWriter FileOutputStream BufferedWriter]))


(defn format-name [name]
  (str (clojure.string/replace name #"[-: ]" "")))

(defn make-filename [tag title postdate]
  (str (format-name (pinyin tag {:tone :without-tone :v-char :with-v})) "-" (format-name (pinyin title {:tone
  :without-tone :v-char :with-v})) "-" (format-name postdate)))

(defn make-postdate []
  (.format (new java.text.SimpleDateFormat "yyyy-MM-dd-HH:mm:ss") (new java.util.Date)))

(defn make-filepath [name]
  (str (io/resource-path) "/upload/blog/file/" name))

(defn make-summary [content]
  (let [len (count content)
	text (util/strip-html-tags content)]
    (if (> len 100) (.substring text 0 180) (str text))))


(defn insert-blog-entry [title tag summary content postdate uuid filepath]
  (clojure.java.jdbc/insert-values
   :blog
   [:title :tag :summary :content :postdate :uuid :filepath]
   [title tag summary content postdate uuid filepath]))


(defn create-blog [title content tag]
 (do (let [summary (make-summary content)
        postdate (make-postdate)
        filepath (clojure.string/replace (make-filepath (make-filename tag title postdate)) #"%5c" "")
        uuid (util/uuid)]
    (jdbc/with-connection
      schema/db-spec
      (jdbc/transaction
       (insert-blog-entry title tag summary content postdate uuid filepath)))
    (if-not (.exists (new java.io.File filepath)) (.createNewFile (new java.io.File filepath)))
    (with-open [fw (java.io.FileWriter. filepath)
                pw (java.io.PrintWriter. fw)]
      (.println pw content)
      (.close pw)
      (.close fw)) (str uuid))))

(defn read-blog-by-uuid [uuid]
  (do 
    (let [sql (str "select * from blog where uuid=" "'" uuid "'")]
       (jdbc/with-connection
         schema/db-spec
         (jdbc/with-query-results rs [sql]
           (take 1 rs))))))

(defn read-blog-list []
  (do
    (let [sql "select * from blog order by id desc"
          result []]
      (jdbc/with-connection
        schema/db-spec
        (jdbc/transaction
         (jdbc/with-query-results rs [sql]
           (into result rs)))))))

(defn del-blog-by-id [id filepath]
  (jdbc/with-connection
    schema/db-spec
    (jdbc/delete-rows :blog ["id=?" id]))
  (let [f (clojure.java.io/file filepath)]
        (if (.exists f)
          (clojure.java.io/delete-file f true)
          (prn nil))))

(defn update-blog [id attr-map]
  (jdbc/with-connection
    schema/db-spec
    (jdbc/update-values
     :blog
     ["id=?" id]
     attr-map))
  (with-open [fw (java.io.FileWriter. (:filepath attr-map))
              pw (java.io.PrintWriter. fw)]
    (.println pw (:content attr-map))
    (.close pw)
    (.close fw))
  "ok")

(defn update-blog-entry [id attr-map]
  (jdbc/update-values
   :blog
   ["id=?" id]
   attr-map))

(defn read-tag-list []
  (let [sql "select * from tag"
        result []]
    (jdbc/with-connection
      schema/db-spec
      (jdbc/with-query-results rs [sql]
        (into result rs)))))
        

(defn read-blog-list-by-tag [tagid]
  (let [sql (str "select * from blog where tag like '% " tagid " %'")
        result []]
    (jdbc/with-connection
      schema/db-spec
      (jdbc/with-query-results rs [sql]
        (into result rs)))))
 
(defn read-tag-by-id [id]
  (let [sql (str "select id,tagname from tag where id =" "'" id "'")]
    (jdbc/with-connection
      schema/db-spec
      (jdbc/with-query-results rs [sql]
        (take 1 rs)))))
