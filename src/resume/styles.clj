(ns resume.styles
  (:require [garden.selectors :as s]
            [garden.stylesheet :as ss :refer [at-media]]))

;;; There are two hacks defined here to work around the lack of classes in markdown.
;;; The first is that the 'i' element is used exclusively in the contact section to
;;; make links explicit in print. The second is that the 'hr' element is invisible
;;; and used throughout the document to force a page break in print.

;; TODO: Fix styles for smaller view ports

(def resume
  [["@page" {:margin "2cm"}]
   [:body {:font-family "Arial, sans-serif"
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
         :margin-bottom "1rem"
         :page-break-inside "avoid"}]
   (at-media {:print :only}
             [:li {:margin-top "0.5rem"
                   :margin-bottom "0.5rem"}])
   [:hr {:visibility "hidden"
         :margin "0"
         :border "0"
         :page-break-after "always"}]
   [:#contact {:margin-top "4rem"
               :margin-bottom "4rem"
               :text-align "center"}
    [:i {:font-style "normal"}]]
   (at-media {:screen :only
              :max-width "799px"}
             [:#contact
              [:p {:font-size "3rem"
                   :line-height "2"}]])
   (at-media {:print :only}
             [:#contact {:margin-top "0"
                         :margin-bottom "1cm"}
              [:i
               [:a:after {:content "\": \" attr(href) \" \""}]]])
   [:#skill-summary
    [:p {:margin-top "2rem"}]
    [:thead {:display "none"}]
    [:tbody {:display "flex"
             :flex-wrap "wrap"
             :justify-content "space-between"}]
    [:tr {:display "flex"
          :flex-basis "45%"
          :justify-content "space-between"}]
    [:td
     [(s/& (s/nth-of-type "2")) {:text-align "right"}]]]
   [:#interesting-reading
    [:h2 {:page-break-before "initial"}]
    [:p {:text-align "center"}]]
   [:#educational-background
    [:h2 {:page-break-before "initial"}]]
   [:#references
    [:h2 {:page-break-before "initial"}]
    [:p {:text-align "center"}]]])
