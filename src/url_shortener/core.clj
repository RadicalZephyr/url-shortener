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
  (GET "/info/:short-url" [short-url]
       (let [url-map (get @url-mapping short-url)]
         (if url-map
           (format "Short URL %s<br>Redirects to: %s<br>Number of hits: %d"
                   short-url (:url url-map) (:hit-count url-map))
           (str "<h1>No short url: '" short-url "' was found to "
                "display information for.</h1>"))))
  (GET "/:short-url" [short-url]
       (let [url-map (get @url-mapping short-url)]
         (if url-map
           (do
             (swap! url-mapping update-hit-count short-url)
             (response/redirect (:url url-map)))
           "<h1>No redirect was found.</h1>"))))

(def app (wrap-params routes))
