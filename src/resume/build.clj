(ns resume.build
  (:require [boot.core :as boot]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [markdown.core :refer [md-to-html-string-with-meta]]))

;; Transformations (Pure)

(defn get-id [doc]
  (-> doc :metadata :id first))

(defn vec->order [v]
  (zipmap v (range)))

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

(defn compile-html-doc [lang head body]
  (str "<!DOCTYPE html>\n"
   "<html lang=\"" lang "\">\n"
   "<head>\n" head "</head>\n"
   "<body>\n" body "</body>\n"
   "</html>\n"))

;; Actions (Impure)

(defn parse-file [file]
  (-> file slurp md-to-html-string-with-meta))

(defn parse-dir [dir opts]
  (->> dir
    (map parse-file)
    (sort-by (:sort opts))
    (map #(wrap-with :div {:id (get-id %)}
                          (:html %)))))

(defn build-doc [in out opts]
  (let [head (cons (title-tag (:title opts))
                   (map style-tag (:styles opts)))
        body (parse-dir in {:sort #(get (:order opts) (get-id %))})
        doc  (compile-html-doc (:lang opts) (apply str head) (apply str body))]
    (spit out doc)))

(defn copy-file [in out]
  (doto out
    io/make-parents
    (spit (slurp in))))

;; Boot Task

(boot/deftask build
  "Build the entire project document."
  []
  (fn build-middleware [next-handler]
    (fn build-handler [fileset]
      (let [tmp (boot/tmp-dir!)
            in-files (boot/input-files fileset)
            md-files (boot/by-re [#"^markup/en/.+\.md$"] in-files)
            css-files (boot/by-re [#"^styles/.+\.css$"] in-files)
            html-out (io/file tmp "index.html")]
        (build-doc (map boot/tmp-file md-files) html-out
          {:lang "en"
           :title "Resume - Shayden Martin"
           :styles (map :path css-files)
           :order (vec->order ["contact"
                               "resume"
                               "professional-experience"
                               "educational-background"
                               "references"])})
        (doseq [css-file css-files]
          (let [css-out (io/file tmp (boot/tmp-path css-file))]
            (copy-file (boot/tmp-file css-file) css-out)))
        (-> fileset
          (boot/add-resource tmp)
          boot/commit!
          next-handler)))))
