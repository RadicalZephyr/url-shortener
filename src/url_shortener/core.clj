(ns url-shortener.core
  (:require [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.route :as route]))


(def url-mapping (atom {}))

(defroutes app
  (GET "/" [] (response/redirect "/index.html"))
  (POST "/" params (str "Got a url" params))
  (route/resources "/")
  (GET "/:url-id" [url-id]
       (let [url (get @url-mapping url-id)]
         (if url
           (response/redirect url)
           "<h1>No redirect was found.</h1>"))))
