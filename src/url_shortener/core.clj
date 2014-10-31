(ns url-shortener.core
  (:require [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.route :as route]))


(def url-mapping (atom {"abcde" "http://www.google.com/search?q=thebestkittenever"}))


(defroutes app
  (GET "/" [] (response/redirect "/index.html"))
  (route/resources "/")
  (GET "/:url-id" [url-id]
       (let [url (get @url-mapping url-id)]
         (if url
           (response/redirect url)
           "<h1>No redirect was found.</h1>"))))
