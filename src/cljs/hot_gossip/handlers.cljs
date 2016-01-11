(ns hot-gossip.handlers
    (:require [re-frame.core :as re-frame]
              [hot-gossip.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
