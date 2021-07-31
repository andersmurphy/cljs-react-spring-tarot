(ns cljs-react-spring-tarot.app
  (:require [helix.core :refer [defnc $]]
            [react-dom :as rdom]
            [react-spring :as spr]))

(defn use-springs [n f]
  (-> (spr/useSprings n (comp clj->js f))
      js->clj))

(defn to [[x y] f]
  (spr/interpolate #js [x y] f))

(def cards
  ["https://upload.wikimedia.org/wikipedia/en/f/f5/RWS_Tarot_08_Strength.jpg" "https://upload.wikimedia.org/wikipedia/en/5/53/RWS_Tarot_16_Tower.jpg" "https://upload.wikimedia.org/wikipedia/en/9/9b/RWS_Tarot_07_Chariot.jpg" "https://upload.wikimedia.org/wikipedia/en/d/db/RWS_Tarot_06_Lovers.jpg" "https://upload.wikimedia.org/wikipedia/en/thumb/8/88/RWS_Tarot_02_High_Priestess.jpg/690px-RWS_Tarot_02_High_Priestess.jpg" "https://upload.wikimedia.org/wikipedia/en/d/de/RWS_Tarot_01_Magician.jpg"])

(defn trans [r s]
  (str "perspective(1500px) rotateX(30deg) rotateY("(/ r 10)"deg) rotateZ("r"deg) scale("s")"))

(defnc app []
  (let [[props] (use-springs
                 (count cards)
                 (fn [i]
                   {:x 0
                    :y (* i -4)
                    :scale 1
                    :rot (- (* (rand) 20) 10)
                    :delay (* i 100)
                    :from {:x 0 :y -1000 :scale 1.5 :rot 0}}))]
    (map-indexed
     (fn [i {:strs [x y rot scale]}]
       ($ spr/animated.div
          {:key i
           :style
           #js {:transform (to [x y] (fn [x y]
                                       (str "translate3d("x"px,"y"px,0)")))}}
          ($ spr/animated.div
             {:style
              #js {:transform       (to [rot scale] trans)
                   :backgroundImage (str "url("(cards i)")")}})))
     props)))

(defn init []
  (rdom/render ($ app) (js/document.getElementById "root")))
