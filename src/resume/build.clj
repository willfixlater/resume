(ns resume.build
  (:require [endophile.core :refer [mp]]
            [endophile.hiccup :refer [to-hiccup]]
            [hiccup.page :refer [html5]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [split]]))

(defn get-name [file]
  (-> file .getName (split #"\.") first))

(defn wrap-with-div
  ([contents]
   (into [:div] contents))
  ([selectors contents]
   (into [(keyword (apply str "div" selectors))] contents)))

(defn parse-file [file]
  (-> file slurp mp to-hiccup))

(defn parse-dir [dir]
  (->> (file-seq dir)
   (filter #(.isFile %))
   (map #(wrap-with-div ["#" (get-name %)]
                        (parse-file %)))))

(defn compile-html-doc [lang head body]
  (html5 {:lang lang}
   (into [:head] head)
   (into [:body] body)))

(defn build-doc
  ([]
   (build-doc "public/index.html"))
  ([out]
   (let [body (-> "resources/markup" as-file parse-dir)
         doc  (compile-html-doc "en" [] body)]
     (spit out doc))))
