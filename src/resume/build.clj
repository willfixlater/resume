(ns resume.build
  (:require [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [markdown.core :refer [md-to-html-string-with-meta]]))

(def doc-order ["contact"
                "resume"
                "professional-experience"
                "educational-background"
                "references"])

(defn index-of [e coll]
  (first (keep-indexed #(when (= e %2) %1) coll)))

(defn get-id [doc]
  (-> doc :metadata :id first))

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

(defn parse-dir [dir opts]
  (->> (file-seq dir)
   (filter #(.isFile %))
   (map parse-file)
   (sort-by (:sort opts))
   (map #(wrap-with :div {:id (get-id %)}
                         (:html %)))))

(defn compile-html-doc [lang head body]
  (str "<!DOCTYPE html>\n"
   "<html lang=\"" lang "\">\n"
   "<head>\n" head "</head>\n"
   "<body>\n" body "</body>\n"
   "</html>\n"))

(defn build-doc [in out]
  (let [body (parse-dir in {:sort #(index-of (get-id %) doc-order)})
        doc  (compile-html-doc "en" "" (apply str body))]
    (spit out doc)))

(boot/deftask build
  "Build the entire project document."
  []
  (let [tmp (boot/tmp-dir!)]
    (fn build-middleware [next-handler]
      (fn build-handler [fileset]
        (let [in-files (boot/input-files fileset)
              mu-files (boot/by-path ["resources/markup"] in-files)
              md-files (boot/by-ext [".md"] mu-files)
              out (io/file tmp "target/index.html")]
          (build-doc md-files out))
        (-> fileset
          (boot/add-resource tmp)
          boot/commit!
          next-handler)))))
