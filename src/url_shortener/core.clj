(ns url-shortener.core
  (:require [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]))

(def url-mapping (atom {}))

(defn shorten-url [url]
  (let [shortened (mod (hash url) 1000)]
    (swap! url-mapping assoc (str shortened) url)
    shortened))

(defroutes routes
  (GET "/" [] (response/redirect "/index.html"))
  (POST "/" [url]
        (let [short (shorten-url url)]
          (str "We shortened your url to: " short @url-mapping)))
  (route/resources "/")
  (GET "/:url-id" [url-id]
       (let [url (get @url-mapping url-id)]
         (if url
           (response/redirect url)
           (str "<h1>No redirect was found.</h1>" @url-mapping)))))

(def app (wrap-params routes))
