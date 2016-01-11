(ns hot-gossip.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [hot-gossip.handlers]
              [hot-gossip.subs]
              [hot-gossip.views :as views]
              [hot-gossip.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init [] 
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
