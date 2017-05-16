(def project 'resume)

(set-env! :source-paths #{"src"}
          :resource-paths #{"resources"}
          :dependencies '[[endophile "RELEASE"]
                          [hiccup "RELEASE"]])

(require '[resume.build :refer [build-doc]])

(deftask build
  "Build the entire project document."
  []
  (build-doc))



