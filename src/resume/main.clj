(ns resume.main
  (:require [clojure.java.io :as io]
            [juxt.dirwatch :as dirwatch]
            [ring.middleware.file :as ring.mw.file]
            [ring.adapter.jetty :as ring.a.jetty]
            [resume.core :refer [generate-markup generate-styles]]
            [resume.styles :as styles]))

(defonce ^:private dev-server (atom nil))

(defonce ^:private dev-watches (atom #{}))

(def ^:private base-config
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
  [config]
  (let [html-out-file (io/file (config :out-path) "index.html")
        md-in-dir (io/file (config :md-in-path) (config :lang))
        md-in-children (file-seq md-in-dir)
        md-in-files (filter #(.isFile %) md-in-children)]
    (generate-markup md-in-files html-out-file
                     {:lang (config :lang)
                      :title (config :title)
                      :style-paths (config :style-paths)
                      :sections (config :sections)})))

(defn- build-css
  [config]
  (doseq [[dest-filepath garden-doc] (config :style-paths)]
    (let [dest-file (io/file (config :out-path) dest-filepath)]
      (generate-styles garden-doc dest-file))))

(defn- build-once
  [config]
  (build-html config)
  (build-css config))

(defn- serve
  [config]
  (let [handler (-> (fn handler [_request]
                      {:status 404
                       :body "<h1>404 Not Found</h1>"})
                    (ring.mw.file/wrap-file (config :out-path)))]
    (ring.a.jetty/run-jetty handler
                            {:port (config :port)
                             :join? false})))

(defn- start-dev-server
  [config]
  (reset! dev-server (serve config)))

(defn- stop-dev-server
  [_config]
  (when dev-server
    (.stop dev-server)))

(defn- start-dev-watches
  [config]
  (let [markup-watch (dirwatch/watch-dir (fn [& _args]
                                           (build-once config))
                                         (io/file (config :md-in-path)
                                                  (config :lang)))
        src-watch (dirwatch/watch-dir (fn [& _args]
                                        (stop-dev-server config)
                                        ;; TODO: Exception handling on reload
                                        (require 'resume.styles :reload)
                                        (require 'resume.core :reload)
                                        (require 'resume.main :reload)
                                        (build-once config)
                                        (start-dev-server config))
                                      (io/file "src/resume"))]
    (reset! dev-watches #{markup-watch src-watch})))

(defn- stop-dev-watches
  [_config]
  (when (seq dev-watches)
    (doseq [watch dev-watches]
      (dirwatch/close-watcher watch))))

(defn- build-serve-&-watch
  [config]
  (build-once config)
  (start-dev-server config)
  (start-dev-watches config)
  nil)

(defn- stop-serve-&-watch
  [config]
  (stop-dev-watches config)
  (stop-dev-server config)
  nil)

;; TODO: Docstrings and metadata for following commands

(def build build-once)

(def dev build-serve-&-watch)

(def stop-dev stop-serve-&-watch)

(comment
  (build base-config)
  (dev base-config)
  (stop-dev base-config)
  )