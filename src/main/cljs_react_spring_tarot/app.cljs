(ns cljs-react-spring-tarot.app
  (:require [helix.core :refer [defnc $]]
            [react-dom :as rdom]
            [helix.hooks :as hks]
            ["@react-spring/web" :as spr]
            [react-use-gesture :as gest]))

(defn use-springs [n f]
  (let [[props api] (-> (spr/useSprings n (comp clj->js f))
                        (js->clj :keywordize-keys true))]

    [props {:start! (fn [f] (.start api (comp clj->js f)))}]))

(defn to [[x y] f]
  (spr/to #js [x y] f))

(defn use-drag
  ([f] (use-drag f {}))
  ([f config]
   (gest/useDrag (comp f #(js->clj % :keywordize-keys true)) config)))

(def cards
  ["https://upload.wikimedia.org/wikipedia/en/f/f5/RWS_Tarot_08_Strength.jpg" "https://upload.wikimedia.org/wikipedia/en/5/53/RWS_Tarot_16_Tower.jpg" "https://upload.wikimedia.org/wikipedia/en/9/9b/RWS_Tarot_07_Chariot.jpg" "https://upload.wikimedia.org/wikipedia/en/d/db/RWS_Tarot_06_Lovers.jpg" "https://upload.wikimedia.org/wikipedia/en/thumb/8/88/RWS_Tarot_02_High_Priestess.jpg/690px-RWS_Tarot_02_High_Priestess.jpg" "https://upload.wikimedia.org/wikipedia/en/d/de/RWS_Tarot_01_Magician.jpg"])

(defn trans [r s]
  (str "perspective(1500px) rotateX(30deg) rotateY("(/ r 10)"deg) rotateZ("r"deg) scale("s")"))

(defnc app []
  (let [[gone set-gone!] (hks/use-state #{})
        [props {:keys [start!]}]
        (use-springs
         (count cards)
         (fn [i]
           {:x 0
            :y (* i -4)
            :scale 1
            :rot (- (* (rand) 20) 10)
            :delay (* i 100)
            :from {:x 0 :y -1000 :scale 1.5 :rot 0}}))
        bind
        (use-drag
         (fn [{[index] :args
               [mx]    :movement
               [xd]  :direction
               :keys [down velocity]}]
           (let [trigger (> velocity 0.2)
                 dir (if (> xd 0) 1 -1)
                 gone (if (and (not down) trigger) (conj gone index) gone)]
             (start!
              (fn [i]
                (when (= i index)
                  {:x (if (gone i)
                        (* (+ 200 (.-innerWidth js/window)) dir)
                        (if down mx 0))
                   :rot (+ (/ mx 100)
                           (if (gone i) (* dir 10 velocity) 0))
                   :scale (if down 1.1 1)
                   :delay nil
                   :config {:friction 50
                            :tension (if down
                                       800
                                       (if (gone i) 200 500))}})))
             (set-gone! gone)
             (when (and (not down) (= (count gone) (count cards)))
               (js/setTimeout
                (fn []
                  (set-gone! #{})
                  (start!
                   (fn [i]
                     {:x 0
                      :y (* i -4)
                      :scale 1
                      :rot (- (* (rand) 20) 10)
                      :delay (* i 100)})))
                600))))
         {:axis "x"})]
    (map-indexed
     (fn [i {:keys [x y rot scale]}]
       ($ spr/animated.div
          {:key i
           :style
           #js {:transform (to [x y] (fn [x y]
                                       (str "translate3d("x"px,"y"px,0)")))}}
          ($ spr/animated.div
             {:style
              #js {:transform       (to [rot scale] trans)
                   :backgroundImage (str "url("(cards i)")")
                   :touchAction "pan-y"}
              & (bind i)})))
     props)))

(defn init []
  (rdom/render ($ app) (js/document.getElementById "root")))
