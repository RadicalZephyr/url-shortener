(ns url-shortener.core
  (:require [ring.util.response :as response]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core  :as c]
            [compojure.route :as route]))

(def url-mapping (atom {}))

(defn has-http [url]
  (re-find #"^https?://" url))

(defn make-url-entry [url]
  {:url url :hit-count 0})

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

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(c/defroutes routes
  (c/GET "/" [] (response/redirect "/index.html"))

  (c/context "/api/v1" []
    (c/POST "/shorten" [url]
      (let [short-url (shorten-url url)]
        (edn-response {:short-url short-url})))

    (c/GET "/info/:short-url" [short-url]
      (if-let [url-map (get @url-mapping short-url)]
           (edn-response url-map)
           (edn-response {:error "No entry found for that short url"
                          :url short-url}
                         404))))

  (c/POST "/shorten" [url]
        (let [short (shorten-url url)]
          (format "We shortened your url to: <a href=\"/s/%s\">/s/%s</a>"
                  short short)))
  (c/GET "/info/:short-url" [short-url]
       (if-let [url-map (get @url-mapping short-url)]
           (format "Short URL %s<br>Redirects to: %s<br>Number of hits: %d"
                   short-url (:url url-map) (:hit-count url-map))
           (str "<h1>No short url: '" short-url "' was found to "
                "display information for.</h1>")))
  (c/GET "/s/:short-url" [short-url]
       (if-let [url-map (get @url-mapping short-url)]
           (do
             (swap! url-mapping update-hit-count short-url)
             (response/redirect (:url url-map)))
           "<h1>No redirect was found.</h1>"))
  (route/resources "/"))

(def app (wrap-params routes))
