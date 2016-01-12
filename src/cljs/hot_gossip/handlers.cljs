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
 :ws/send
 (fn [db [_  command & data]]
   (debugf "Sending: %s %s" command data)
   (ws/chsk-send! [command data])))
