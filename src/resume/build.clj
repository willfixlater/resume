(ns resume.build
  (:require [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]]
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

(defn title-tag [title]
  (str
    "<title>"
    title
    "</title>\n"))

(defn style-tag [path]
  (str
    "<link "
    "rel=\"stylesheet\" "
    "type=\"text/css\" "
    "href=\"" path "\">\n"))

(defn parse-file [file]
  (-> file slurp md-to-html-string-with-meta))

(defn parse-dir [dir opts]
  (->> dir
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

(defn build-doc [in out opts]
  (let [head (cons (title-tag "Resume - Shayden Martin")
                   (map style-tag (:styles opts)))
        body (parse-dir in {:sort #(index-of (get-id %) doc-order)})
        doc  (compile-html-doc "en" (apply str head) (apply str body))]
    (spit out doc)))

(defn copy-file [in out]
  (doto out
    io/make-parents
    (spit (slurp in))))

(boot/deftask build
  "Build the entire project document."
  []
  (let [tmp (boot/tmp-dir!)]
    (fn build-middleware [next-handler]
      (fn build-handler [fileset]
        (let [in-files (boot/input-files fileset)
              md-files (boot/by-re [#"^markup/.+\.md$"] in-files)
              css-files (boot/by-re [#"^styles/.+\.css$"] in-files)
              html-out (io/file tmp "index.html")]
          (build-doc (map boot/tmp-file md-files) html-out
            {:styles (map :path css-files)})
          (doseq [css-file css-files]
            (let [css-out (io/file tmp (boot/tmp-path css-file))]
              (copy-file (boot/tmp-file css-file) css-out))))
        (-> fileset
          (boot/add-resource tmp)
          boot/commit!
          next-handler)))))
