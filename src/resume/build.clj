(ns resume.build
  (:require [endophile.core :refer [mp]]
            [endophile.hiccup :refer [to-hiccup]]
            [hiccup.page :refer [html5]]
            [clojure.java.io :refer [resource]]))

(defn parse-resume
  ([]
   (parse-resume (resource "markup/resume.md")))
  ([path]
   (-> path slurp mp to-hiccup)))

(defn compile-html-doc [lang head body]
  (html5 {:lang lang}
   (into [:head] head)
   (into [:body] body)))

(defn build-doc
  ([]
   (build-doc "public/index.html"))
  ([out]
   (let [resume (into [:div#resume] (parse-resume))
         doc    (compile-html-doc "en" [] [resume])]
     (spit out doc))))
