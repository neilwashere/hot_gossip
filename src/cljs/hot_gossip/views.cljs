(ns hot-gossip.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as r]))

(defn get-val [id]
  (.-value (.getElementById js/document id)))




(defn topic [topic]
  (let [messages (re-frame/subscribe [:topic topic])]
    (fn [topic]
      [:div
       [:p "Listening for " topic]
       [:textarea#output {:style "width: 100%; height: 200px;"}
        (for [m @messages]
          (str m "\n"))]])))

(defn topics-list []
  (let [topics (re-frame/subscribe [:topics])]
    [:div
     [:h1 "Topics watched"]
      (for [topic @topics]
        [topic-watcher topic])]))

(defn register []
  (let [topic (r/atom nil)]
    [:p
     [:input#input-register {:type :text
                             :value @topic
                             :placeholder "topic name"
                             :on-change #(reset! topic (-> % .-target .-value))}]
     [:button#btn-register
      {:type "button"
       :on-click #(re-frame/dispatch [:register-topic @topic])}
      "Listen!"]]))

(defn main-panel []
  (let [connected? (re-frame/subscribe [:ws/connected])
        name (re-frame/subscribe [:name])]
    (if (and @connected? @name)
      (fn []
        [:div "Hello from " @name]
        [:div [register]]
        [:div#outputs [topics-list]]))))
