(ns hot-gossip.handlers
    (:require [re-frame.core :as re-frame]
              [hot-gossip.db :as db]
              [hot-gossip.ws :as ws]
              [taoensso.encore :as encore :refer [debugf]]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/register-handler
 :ws/register-topic
 (fn [db [_  topic]]
   (when-not (-> db :topics topic)
     (ws/chsk-send! [:topic/register topic])
     (assoc-in db [:topics topic] []))))

(re-frame/register-handler
 :ws/connected
 (fn [db [_ connected?]]
   (if connected? (ws/chsk-send! [:state/sync]))
   (assoc db :ws/connected connected?)))

(re-frame/register-handler
 :topic/event
 (fn [db [_ event]]
   (debugf "got topic %s" event)
   (update-in db [:topics (:topic event)] conj (:data event))))
