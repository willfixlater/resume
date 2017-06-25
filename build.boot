(def project 'resume)

(set-env! :source-paths #{"src"}
          :resource-paths #{"resources"
                            "target"}
          :dependencies '[[pandeiro/boot-http "0.8.3"]
                          [markdown-clj "0.9.99"]])

(require '[pandeiro.boot-http :refer [serve]])
(require '[resume.build :refer [build]])

(deftask once
  "Build the project once."
  []
  (build))

(deftask dev
  "Rebuild and serve for development."
  []
  (comp
    (build)
    (serve :dir "public")
    (watch)))
