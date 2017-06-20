(ns resume.build
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [clojure.java.io :refer [as-file]]
            [clojure.string :refer [split]]))

(def doc-order ["contact"
                "resume"
                "professional-experience"
                "educational-background"
                "references"])

(defn index-of [e coll]
  (first (keep-indexed #(when (= e %2) %1) coll)))

(defn wrap-with
  ([elem contents]
   (wrap-with elem {} contents))
  ([elem attrs contents]
   (str "<" (name elem)
     (if-let [ids (:id attrs)]
       (str " id=\"" ids "\""))
     (if-let [classes (:class attrs)]
       (str " class=\"" classes "\""))
     ">\n" contents "\n</" (name elem) ">\n")))

(defn parse-file [file]
  (-> file slurp md-to-html-string-with-meta))

(defn parse-dir [dir]
  (->> (file-seq dir)
   (filter #(.isFile %))
   (map parse-file)
   (sort-by #(index-of (-> % :metadata :id first) doc-order))
   (map #(wrap-with :div {:id (-> % :metadata :id first)}
                         (:html %)))))

(defn compile-html-doc [lang head body]
  (str "<!DOCTYPE html>\n"
   "<html lang=\"" lang "\">\n"
   "<head>\n" head "</head>\n"
   "<body>\n" body "</body>\n"
   "</html>\n"))

(defn build-doc
  ([]
   (build-doc "public/index.html"))
  ([out]
   (let [body (-> "resources/markup" as-file parse-dir)
         doc  (compile-html-doc "en" "" (apply str body))]
     (spit out doc))))
