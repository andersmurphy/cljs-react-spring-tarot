(ns cljs-react-spring-tarot.app
  (:require [helix.core :refer [defnc $]]
            [react-dom :as rdom]
            [helix.hooks :refer [use-state]]
            ["@react-spring/web" :as spr]
            [react-use-gesture :as gest]))

(defn use-springs [n f]
  (let [[props api] (-> (spr/useSprings n (comp clj->js f))
                        (js->clj :keywordize-keys true))]
    [props {:start! (fn [f] (.start api (comp clj->js f)))}]))

(defn use-drag
  ([f] (use-drag f {}))
  ([f config]
   (gest/useDrag (comp f #(js->clj % :keywordize-keys true)) config)))

(def card-list
  ["https://upload.wikimedia.org/wikipedia/en/f/f5/RWS_Tarot_08_Strength.jpg"
   "https://upload.wikimedia.org/wikipedia/en/5/53/RWS_Tarot_16_Tower.jpg"
   "https://upload.wikimedia.org/wikipedia/en/9/9b/RWS_Tarot_07_Chariot.jpg"
   "https://upload.wikimedia.org/wikipedia/en/d/db/RWS_Tarot_06_Lovers.jpg"
   "https://upload.wikimedia.org/wikipedia/en/thumb/8/88/RWS_Tarot_02_High_Priestess.jpg/690px-RWS_Tarot_02_High_Priestess.jpg"
   "https://upload.wikimedia.org/wikipedia/en/d/de/RWS_Tarot_01_Magician.jpg"])

(defn url [url]
  (str "url("url")"))

(defnc app []
  (let [[{:keys [flipped gone cards]} set-state!]
        (use-state {:flipped #{}
                    :gone #{}
                    :cards (shuffle card-list)})
        [props {:keys [start!]}]
        (use-springs
         (count cards)
         (fn [i]
           {:x 0
            :y (* i -4)
            :scale 1
            :rotateZ (- (* (rand) 20) 10)
            :rotateY 0
            :delay (* i 100)
            :from {:x 0 :y -1000 :scale 1.5}}))
        bind
        (use-drag
         (fn [{[index] :args
               [mx]    :movement
               [vx]    :velocities
               :keys   [down]}]
           (let [trigger (> (js/Math.abs vx) 0.2)
                 dir (if (> vx 0) 1 -1)
                 gone (if (and (not down) trigger) (conj gone index) gone)
                 flipped (if (and down (not (flipped index)))
                           (conj flipped index) flipped)]
             (start!
              (fn [i]
                (when (= i index)
                  {:x (if (gone i)
                        (* (+ 200 (.-innerWidth js/window)) dir)
                        (if down mx 0))
                   :rotateZ (+ (/ mx 100)
                               (if (gone i) (* 10 vx) 0))
                   :rotateY (if (flipped i) 180 0)
                   :scale (if down 1.1 1)
                   :delay nil
                   :config {:friction 50
                            :mass 3
                            :tension (if down
                                       800
                                       (if (gone i) 200 500))}})))
             (set-state! #(assoc % :gone gone
                                 :flipped flipped))
             (when (and (not down) (= (count gone) (count cards)))
               (js/setTimeout
                (fn []
                  (set-state! #(assoc % :gone #{}
                                      :flipped #{}
                                      :cards (shuffle cards)))
                  (start!
                   (fn [i]
                     {:x 0
                      :y (* i -4)
                      :scale 1
                      :rotateY 0
                      :rotateZ (- (* (rand) 20) 10)
                      :delay (* i 100)})))
                600))))
         {:axis "x"})]
    (map-indexed
     (fn [i {:keys [x y scale rotateZ rotateY]}]
       ($ spr/animated.div
          {:key i
           :style
           #js {:x x :y y}}
          ($ spr/animated.div
             {:style
              #js {:rotateZ rotateZ
                   :rotateY rotateY
                   :scale scale
                   :backgroundImage (url (card-list i))
                   :touchAction "pan-y"}
              & (bind i)})))
     props)))

(defn init []
  (rdom/render ($ app) (js/document.getElementById "root")))
