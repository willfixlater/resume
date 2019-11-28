(def project 'resume)

(set-env! :source-paths #{"src" "resources"}
          :dependencies '[[pandeiro/boot-http "0.8.3"]
                          [markdown-clj "0.9.99"]
                          [hiccup "1.0.5"]
                          [garden "1.3.5"]])

(require '[pandeiro.boot-http :refer [serve]]
         '[resume.boot :refer [reload-source build]])

(deftask once
  "Build the project once."
  []
  (comp
   (build)
   (target)))

(deftask dev
  "Rebuild and serve for development."
  []
  (comp
   (watch)
   (reload-source)
   (build)
   (target)
   (serve :dir "target")))
