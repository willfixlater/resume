(ns resume.main
  (:require [clojure.java.io :as io]
            [juxt.dirwatch :as dirwatch]
            [ring.middleware.file :as ring.mw.file]
            [ring.adapter.jetty :as ring.a.jetty]
            [resume.core :refer [generate-markup generate-styles]]
            [resume.styles :as styles]))

(defonce ^:private dev-server (atom nil))

(defonce ^:private dev-watches (atom #{}))

(def ^:private default-opts
  {:port 3300
   :lang "en"
   :out-path "target"
   :md-in-path "resources/markup"
   :style-paths {"styles/resume.css" styles/resume}
   :title "Resume - Shayden Martin"
   :sections ["contact"
              "resume"
              "skill-summary"
              "professional-experience"
              "courses-and-certification"
              "educational-background"
              "references"]})

(defn- build-html
  [opts]
  (let [html-out-file (io/file (opts :out-path) "index.html")
        md-in-dir (io/file (opts :md-in-path) (opts :lang))
        md-in-children (file-seq md-in-dir)
        md-in-files (filter #(.isFile %) md-in-children)]
    (generate-markup md-in-files html-out-file
                     {:lang (opts :lang)
                      :title (opts :title)
                      :style-paths (opts :style-paths)
                      :sections (opts :sections)})))

(defn- build-css
  [opts]
  (doseq [[dest-filepath garden-doc] (opts :style-paths)]
    (let [dest-file (io/file (opts :out-path) dest-filepath)]
      (generate-styles garden-doc dest-file))))

(defn- build-once
  [opts]
  (build-html opts)
  (build-css opts))

(defn- serve
  [opts]
  (let [handler (-> (fn handler [_request]
                      {:status 404
                       :body "<h1>404 Not Found</h1>"})
                    (ring.mw.file/wrap-file (opts :out-path)))]
    (ring.a.jetty/run-jetty handler
                            {:port (opts :port)
                             :join? false})))

(defn- start-dev-server
  [opts]
  (reset! dev-server (serve opts)))

(defn- stop-dev-server
  [_opts]
  (when-let [server @dev-server]
    (.stop server)))

(defn- start-dev-watches
  [opts]
  (let [markup-watch (dirwatch/watch-dir (fn [& _args]
                                           (build-once opts))
                                         (io/file (opts :md-in-path)
                                                  (opts :lang)))
        src-watch (dirwatch/watch-dir (fn [& _args]
                                        (stop-dev-server opts)
                                        ;; TODO: Exception handling on reload
                                        ;; TODO: Programmatically determine namespaces that require reloading
                                        (require 'resume.styles :reload)
                                        (require 'resume.core :reload)
                                        (require 'resume.main :reload)
                                        (build-once opts)
                                        (start-dev-server opts))
                                      (io/file "src/resume"))]
    (reset! dev-watches #{markup-watch src-watch})))

(defn- stop-dev-watches
  [_opts]
  (when-let [watches (seq @dev-watches)]
    (doseq [watch watches]
      (dirwatch/close-watcher watch))))

(defn- build-serve-&-watch
  [opts]
  (build-once opts)
  (start-dev-server opts)
  (start-dev-watches opts)
  nil)

(defn- stop-serve-&-watch
  [opts]
  (stop-dev-watches opts)
  (stop-dev-server opts)
  nil)

;; TODO: Docstrings and metadata for following commands

(def build build-once)

(def dev build-serve-&-watch)

(def stop-dev stop-serve-&-watch)

(comment
  (build default-opts)
  (dev default-opts)
  (stop-dev default-opts)
  )