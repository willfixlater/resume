(ns resume.boot
  (:require [boot.core :as boot]
            [clojure.java.io :as io]
            [resume.core :refer [build-doc copy-file]]))

(boot/deftask build-html
  "Build the project's html file(s)."
  []
  (fn middleware [next-handler]
    (fn handler [fileset]
      (let [tmp (boot/tmp-dir!)
            in-files (boot/input-files fileset)
            md-files (boot/by-re [#"^markup/en/.+\.md$"] in-files)
            css-files (boot/by-re [#"^styles/.+\.css$"] in-files)
            html-out (io/file tmp "index.html")]
        (build-doc (map boot/tmp-file md-files) html-out
          {:lang "en"
           :title "Resume - Shayden Martin"
           :style-paths (map :path css-files)
           :sections ["contact"
                      "resume"
                      "skill-summary"
                      "professional-experience"
                      "educational-background"
                      "references"]})
        (-> fileset
          (boot/add-resource tmp)
          next-handler)))))

(boot/deftask build-css
  "Build the project's css file(s)."
  []
  (fn middleware [next-handler]
    (fn handler [fileset]
      (let [tmp (boot/tmp-dir!)
            in-files (boot/input-files fileset)
            css-files (boot/by-re [#"^styles/.+\.css$"] in-files)]
        (doseq [css-file css-files]
          (let [css-out (io/file tmp (boot/tmp-path css-file))]
            (copy-file (boot/tmp-file css-file) css-out)))
        (-> fileset
            (boot/add-resource tmp)
            next-handler)))))

(boot/deftask commit
  "Commit the fileset."
  []
  (fn middleware [next-handler]
    (fn handler [fileset]
      (next-handler (boot/commit! fileset)))))

(boot/deftask build
  "Build all of the project files."
  []
  (comp (build-html)
        (build-css)
        (commit)))
