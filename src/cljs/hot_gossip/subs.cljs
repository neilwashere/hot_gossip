(ns hot-gossip.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]
              [taoensso.encore :as encore :refer [debugf]]))

(re-frame/register-sub
 :name
 (fn [db]
   (reaction (:name @db))))

(re-frame/register-sub
 :ws/connected
 (fn [db]
   (reaction (:ws/connected @db))))

(re-frame/register-sub
 :topics
 (fn [db]
   (reaction (:topics @db))))

(re-frame/register-sub
 :topic
 (fn [db [_ topic]]
   (debugf "looking for topic %s" topic)
   (reaction (-> @db
                 :topics
                 topic))))
