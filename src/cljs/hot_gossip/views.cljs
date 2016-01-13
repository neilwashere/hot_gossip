(ns hot-gossip.views
  (:require [re-frame.core :as re-frame]
            [taoensso.encore :as encore :refer [debugf]]
            [reagent.core :as r]))

(defn topic-watcher [topic]
  (let [messages (re-frame/subscribe [:topic topic])]
    (fn [topic]
      (debugf "rendering topic %s" topic)
      [:div
       [:p "Listening for " (str  topic)]
       [:textarea.topic-output
        {:value
         (for [m @messages]
           (str m "\n"))}]])))

(defn topics-list []
  (let [topics (re-frame/subscribe [:topics])]
    (fn []
      [:div
       [:h1 "Topics watched"]
       (for [[topic _] @topics]
         ^{:key topic} [:div [topic-watcher topic]])])))

(defn register []
  (let [topic (r/atom nil)]
    (fn []
      [:p
       [:input#input-register {:type :text
                               :value @topic
                               :placeholder "topic name"
                               :on-change #(reset! topic (-> % .-target .-value))}]
       [:button#btn-register
        {:type "button"
         :on-click #(re-frame/dispatch [:ws/register-topic (keyword @topic)])}
        "Listen!"]])))

(defn main-panel []
  (let [connected? (re-frame/subscribe [:ws/connected])
        name (re-frame/subscribe [:name])]
    (if (and @connected? @name)
      (fn []
        [:div
         [:h1  "Hello from " @name]
         [:div [register]]
         [:div#outputs [topics-list]]]))))
