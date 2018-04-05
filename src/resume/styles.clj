(ns resume.styles
  (:require [garden.core :refer [css]]
            [garden.selectors :as s]
            [garden.stylesheet :refer [at-media]]
            [garden.units :as u]
            [clojure.java.io :as io]))

(def resume
  [[:body {:font-family "Arial, sans-serif"
           :font-size "22px"}]
   (at-media {:screen :only}
             [:body {:max-width "700px"
                     :margin-left "auto"
                     :margin-right "auto"
                     :margin-bottom "20%"}])
   (at-media {:screen :only
              :max-width "799px"}
             [:body {:font-size "40px"
                     :max-width "none"
                     :margin-left "3rem"
                     :margin-right "3rem"}])
   (at-media {:print :only}
             [:body {:font-size "18px"}]
             [:a {:color "initial"
                  :text-decoration "none"}])
   [:h1 :h2 {:margin-top "4rem"
             :margin-bottom "0"
             :text-align "center"}]
   (at-media {:print :only}
             [:h1 :h2 {:margin-top "2cm"}])
   [:h2 {:page-break-before "always"}]
   [:p {:margin-top "1rem"
        :margin-bottom "2rem"
        :line-height "1.5"
        :page-break-inside "avoid"}]
   [:ul {:page-break-inside "avoid"}]
   [:li {:margin-top "1rem"
         :magin-bottom "1rem"
         :page-break-inside "avoid"}]
   (at-media {:print :only}
             [:li {:margin-top "0.5rem"
                   :margin-bottom "0.5rem"}])
   [:hr {:visibility "hidden"
         :margin "0"
         :border "0"
         :page-break-after "always"}]])

(def ^:dynamic *active-garden-docs* {"resume.css" resume})

(defn build [out-dir {:keys [dest-dir] :as opts}]
  (let [garden-docs (seq *active-garden-docs*)]
    (doseq [[dest-filename garden-doc] garden-docs]
      (let [out (io/file out-dir (str dest-dir dest-filename))]
        (doto out
          io/make-parents
          (spit (apply css garden-doc)))))))
