(ns url-shortener.core
  (:require [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]))

(def url-mapping (atom {}))

(defn has-http [url]
  (re-find #"^https?://" url))

(defn make-url-entry [url]
  {:url url :hit-count 0})

(defn get-long-from-short-url [short-url]
  (:url (get @url-mapping short-url)))

(defn get-hit-count [short-url]
  (:hit-count (get @url-mapping short-url)))

(defn update-hit-count [url-mapping short-url]
  (let [url-record   (get url-mapping short-url)
        hit-count (:hit-count url-record)
        updated-record   (assoc url-record :hit-count (inc hit-count))]
    (assoc url-mapping short-url updated-record)))

(defn shorten-url [url]
  (let [url (if (has-http url)
              url
              (str "http://" url))
        shortened (mod (hash url) 1000)]
    (swap! url-mapping assoc (str shortened) (make-url-entry url))
    shortened))

(defroutes routes
  (GET "/" [] (response/redirect "/index.html"))
  (POST "/" [url]
        (let [short (shorten-url url)]
          (str "We shortened your url to: " short)))
  (route/resources "/")
  (GET "/:url-id" [url-id]
       (let [url (get @url-mapping url-id)]
         (if url
           (response/redirect url)
           "<h1>No redirect was found.</h1>"))))

(def app (wrap-params routes))
