(ns resume.main
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [juxt.dirwatch :as dirwatch]
            [ring.middleware.file :as ring.mw.file]
            [ring.adapter.jetty :as ring.a.jetty]
            [resume.core :refer [write-markdown-files write-garden-clj]]
            [resume.styles :as styles]))

(defonce ^:private dev-server (atom nil))

(defonce ^:private dev-watches (atom #{}))

(def ^:private default-opts
  {:port 3300
   :out-path "target"
   :markup {:index
            {:out-path "index.html"
             :title "Resume - Shayden Martin"
             :lang "en"
             :styles [:resume]
             :markdown/files
             ["resources/markup/en/contact.md"
              "resources/markup/en/resume.md"
              "resources/markup/en/skill_summary.md"
              "resources/markup/en/professional_experience.md"
              "resources/markup/en/courses_and_certification.md"
              "resources/markup/en/educational_background.md"
              "resources/markup/en/references.md"]}}
   :styles {:resume
            {:out-path "styles/resume.css"
             :garden/clj styles/resume}}
   :watches {:build {:watch-paths ["resources/markup"]}
             :reload {:watch-paths ["src/resume"]
                      :reload-nss ['resume.styles
                                   'resume.core
                                   'resume.main]}}})

(def supported-markup-src-types
  ^:private
  #{:markdown/files})

(def supported-styles-src-types
  ^:private
  #{:garden/clj})

(defn- validate-src-types-in-opts [supported-src-types opts]
  (let [found-src-types (set/intersection supported-src-types
                                          (set (keys opts)))]
    (when (= 0 (count found-src-types))
      (throw (ex-info "Could not find a supported src type key"
                      {:opts opts
                       :supported-keys supported-styles-src-types})))
    (when (< 1 (count found-src-types))
      (throw (ex-info "Found more than one src type key"
                      {:opts opts
                       :supported-keys supported-styles-src-types})))))

(defn- build-html
  [opts]
  (doseq [[_ markup-opts] (opts :markup)]
    (validate-src-types-in-opts supported-markup-src-types markup-opts)
    (cond
      (markup-opts :markdown/files)
      (let [out-file (io/file (opts :out-path) (markup-opts :out-path))
            in-files (map io/file (markup-opts :markdown/files))
            style-paths (for [id (markup-opts :styles)]
                          (-> opts :styles id :out-path))]
        (write-markdown-files out-file in-files
                              {:title (markup-opts :title)
                               :lang (markup-opts :lang)
                               :style-paths style-paths})))))

(defn- build-css
  [opts]
  (doseq [[_ styles-opts] (opts :styles)]
    (validate-src-types-in-opts supported-styles-src-types styles-opts)
    (cond
      (styles-opts :garden/clj)
      (let [out-file (io/file (opts :out-path) (styles-opts :out-path))]
        (write-garden-clj out-file (styles-opts :garden/clj))))))

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
  (let [markup-watch (apply dirwatch/watch-dir
                            (fn [& _args]
                              (build-once opts))
                            (map io/file
                                 (-> opts :watches :build :watch-paths)))
        src-watch (apply dirwatch/watch-dir
                         (fn [& _args]
                           (stop-dev-server opts)
                           ;; TODO: Exception handling on reload
                           (doseq [ns (-> opts :watches :reload :reload-nss)]
                             (require ns :reload))
                           (build-once opts)
                           (start-dev-server opts))
                         (map io/file
                              (-> opts :watches :reload :watch-paths)))]
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