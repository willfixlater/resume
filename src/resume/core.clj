(ns resume.core
  (:require [clojure.java.io :as io]
            [markdown.core :refer  [md-to-html-string-with-meta]
                           :rename {md-to-html-string-with-meta parse-md}]
            [hiccup.page :refer [html5]]))

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

(defn build-doc
  "Takes a sequence of input (markdown) files, an output file and a map of options.
  Builds an html document from the input files and spits it into the output file."
  [ins out {:keys [sections title style-paths lang] :as opts}]
  (let [doc-order (zipmap sections (range))
        head [:head [:title title] (map style-tag style-paths)]
        body [:body (mds->html (map (comp parse-md slurp) ins)
                               {:filter (comp (set sections) doc-id)
                                :sort   (comp (partial get doc-order) doc-id)})]]
    (doto out
      io/make-parents
      (spit (html5 {:lang lang}
                   head
                   body)))))

(defn copy-file [in out]
  (doto out
    io/make-parents
    (spit (slurp in))))
