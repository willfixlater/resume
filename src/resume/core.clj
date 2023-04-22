(ns resume.core
  (:require [clojure.java.io :as io]
            [markdown.core :refer  [md-to-html-string-with-meta]
                           :rename {md-to-html-string-with-meta parse-md}]
            [hiccup.page :refer [html5]]
            [garden.core :refer [css]]))

;; Pure

(defn doc-id [md]
  (-> md :metadata :id first))

(defn style-tag [path]
  [:link {:rel  "stylesheet"
          :type "text/css"
          :href path}
    nil])

(defn md->div [md]
  [:div {:id (doc-id md)} (:html md)])

(defn mds->html
  "Takes a sequences of parsed markdown documents and returns an html document."
  [mds opts]
  (->> mds
       (filter (:filter opts))
       (sort-by (:sort opts))
       (map md->div)))

;; Impure

(defn generate-markup
  "Takes a sequence of input (markdown) files, an output file and a map of options.
  Builds an html document from the input files and spits it into the output file."
  [md-files out {:keys [sections title style-paths lang] :as _opts}]
  (let [doc-order (zipmap sections (range))
        head [:head
              [:title title]
              [:meta {:charset "utf-8"}]
              (map style-tag (keys style-paths))]
        body [:body (mds->html (map (comp parse-md slurp) md-files)
                               {:filter (comp (set sections) doc-id)
                                :sort   (comp doc-order doc-id)})]]
    (doto out
      io/make-parents
      (spit (html5 {:lang lang}
                   head
                   body)))
    nil))

(defn generate-styles
  "Takes a map of destination filenames to garden documents, an output directory
  and a map of options. Compiles the garden docs into css and spits them into the
  file with the supplied filename under the output directory."
  [garden-doc out]
  (doto out
    io/make-parents
    (spit (apply css garden-doc)))
  nil)