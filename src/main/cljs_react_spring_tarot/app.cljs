(ns cljs-react-spring-tarot.app
  (:require [helix.core :refer [defnc $]]
            [helix.hooks :as hooks]
            [helix.dom :as d]
            [react-dom :as rdom]))

(defnc greeting
  "A component which greets a user."
  [{:keys [name]}]
  (d/div "Hello, " (d/strong name) "!"))

(defnc app []
  (let [[state set-state] (hooks/use-state {:name "Helix User"})]
    (d/div
     (d/h1 "Welcome!")
     ($ greeting {:name (:name state)})
     (d/input {:value (:name state)
               :on-change
               #(set-state assoc :name (.. % -target -value))}))))

(defn init []
  (rdom/render ($ app) (js/document.getElementById "app")))
