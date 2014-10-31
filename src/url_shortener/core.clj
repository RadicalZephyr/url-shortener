(ns url-shortener.core
  (:require [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.route :as route]))


(def url-mapping (atom {"abcde" "http://www.google.com/search?q=thebestkittenever"}))


(defroutes app
  (GET "/" [] "<h1>Hello Url Shortening World</h1>")
  (GET "/:id" [id]
       (let [url (get @url-mapping id)]
         {:status 302
          :headers {"Location" url}
          :body ""})))
