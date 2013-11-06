(ns myapp.routes.home
  (:use [compojure.core]
        [noir.request]
        [ring.util.response])
  (:require [myapp.views.layout :as layout]
            [myapp.util :as util]
            [myapp.models.blog :as blog]
            [myapp.models.user :as user]
            [taoensso.timbre :as timbre]
            [noir.session :as session])
  (:import [java.io PrintWriter]))


(defn home-page []
  (let [result (blog/read-blog-list)
        tags (blog/read-tag-list)]
    (layout/render
     "home.html" {:blogs result :tags tags :username (session/get :username) :islogin (util/islogin)})))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))

(defn new-blog-page1 []
  (layout/render "new-blog.html" {:username (session/get :username) :islogin (util/islogin)}))

(defn new-blog-page
  ([] (new-blog-page nil))
  ([uuid]
     (if (nil? uuid)
       (let [tags (blog/read-tag-list)]
         (layout/render "new-blog.html" {:username (session/get :username) :tags tags :islogin (util/islogin)}))
       (let [rs (blog/read-blog-by-uuid uuid)
             tags (blog/read-tag-list)
             mytags (re-seq #"\w+" (:tag (first rs)))]
         (layout/render "edit-blog.html" {:blog (first rs) :tags tags :mytags mytags :username (session/get :username) :islogin (util/islogin)})))))


(defn blog-detail-page [uuid]
  (let [rs (blog/read-blog-by-uuid uuid)
        tags (blog/read-tag-list)
        mytags (re-seq #"\w+" (:tag (first rs)))]
    (layout/render "page_blog_item.html" {:blog (first rs) :tags tags :mytags mytags :username (session/get :username) :islogin (util/islogin)})))

(defn blog-list-page []
  (let [result (blog/read-blog-list)
        tags (blog/read-tag-list)]
    (layout/render "home.html" {:blogs result :tags tags :username (session/get :username) :islogin (util/islogin)})))

(defn blog-list-by-tag [tagid]
  (let [result (blog/read-blog-list)
        tags (blog/read-tag-list)
        blogs (blog/read-blog-list-by-tag tagid)]
    (layout/render "home.html" {:blogs blogs :tags tags :username (session/get :username) :islogin (util/islogin)})))
  
(defn add-to-blogs [result tagid]
  (loop [blogs [] result result]
    (if (nil? (seq result))
      blogs
      (recur (if-not (get (:tag (first result)) tagid) (conj blogs (first result))) (rest result)))))
               
(defn save-blog [title content tag]
  (if-not (and (not (empty? title)) (not (empty? content)))
    (redirect "/new-blog")
    (let [uuid (blog/create-blog title content tag)]
      (redirect (str "/blog-detail?uuid=" uuid)))))

(defn delete-blog [id filepath]
  (if (blog/del-blog-by-id id filepath)
    (redirect "/")
    (redirect "/page-404")))

(defn update-blog [id title tag content filepath uuid]
  (let [summary (blog/make-summary content)
        attr-map  (conj {}
                        (hash-map
                         :title title
                         :summary summary
                        :content content
                         :filepath filepath
                         :uuid uuid
                         :tag (str tag "")
                         ))]
    (if (blog/update-blog id attr-map)
      (redirect (str "/blog-detail?uuid=" uuid))
      (redirect "/page-404"))))
    
(defn page-404 []
  (layout/render "page-404.html"))

(defn csdn-page []
  (layout/render "csdn-blog.html" {:username (session/get :username) :islogin (util/islogin)}))

(defn login [username password] 
  (if (.equals "ok" (user/login username password))
         (session/put! :username username)
         nil)
  (let [islogin (util/islogin)
        result (if islogin "logined" "loginerror")]
        (do (prn (str "login result: " result)))
        (str result)))
 

(defn logout []
  (session/put! :username nil)
  (do (prn "log out"))
  (redirect "/"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/new-blog" [] (new-blog-page))
  (POST "/create-blog" [title content tag] (save-blog title content tag)) 
  (GET "/blog-detail" [uuid] (blog-detail-page uuid))
  (GET "/bloglist" [] (blog-list-page))
  (GET "/read-blog-by-tag" [tagid] (blog-list-by-tag tagid))
  (GET "/del-blog" [id filepath] (delete-blog id filepath))
  (GET "/edit-blog" [uuid] (new-blog-page uuid))
  (POST "/update-blog" [id title tag content filepath uuid] (update-blog id title tag content filepath uuid))
  (GET "/page-404" [] (page-404))
  (GET "/csdn-blog" [] (csdn-page))
  (POST "/login" [username password] (login username password))
  (GET "/logout" [] (logout)))



