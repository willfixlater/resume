(ns resume.build
  (:require [hickory.core :refer [parse-fragment as-hiccup]]
            [hickory.render :refer [hiccup-to-html]]
            [clojure.java.io :refer [resource]]))

(defn parse-resume
  ([]
   (parse-resume (resource "markup/resume.html")))
  ([path]
   (->> (slurp path)
        parse-fragment
        (map as-hiccup))))

(defn compile-html-doc [& body]
  (list
   "<!DOCTYPE html>\n"
   [:html
    [:head]
    (into [:body] body)]))

(defn build-doc
  ([]
   (build-doc "public/index.html"))
  ([out]
   (let [resume (into [:div#resume] (parse-resume))
         doc    (-> resume compile-html-doc hiccup-to-html)]
     (spit out doc))))
