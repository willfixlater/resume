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

;; Impure

(defn write-markdown-files!
  "Takes an output file, a sequence of input (markdown) files and a map of
   options. Builds an html document from the input files and spits it into the
   output file."
  [out-file in-files {:keys [lang title style-paths] :as _opts}]
  (let [head [:head
              [:title title]
              [:meta {:charset "utf-8"}]
              (map style-tag style-paths)]
        body [:body (map (comp md->div parse-md slurp) in-files)]]
    (doto out-file
      io/make-parents
      (spit (html5 {:lang lang}
                   head
                   body)))
    nil))

(defn write-garden-clj!
  "Takes an output file, a garden document and a map of options. Compiles the
   garden doc into css and spits it into the output file."
  ([out-file garden-clj]
   (write-garden-clj! out-file garden-clj nil))
  ([out-file garden-clj _opts]
   (doto out-file
     io/make-parents
     (spit (apply css garden-clj)))
   nil))