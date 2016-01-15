(ns hot-gossip.ws
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn (fn [ring-req] "hard-code-for-test")})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defn broadcast
  "Broadcast data to all connected clients"
  [data]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid data)))

(defonce shared-db (atom {:topics {}}))

(defn topic-stream [topic]
  (tubelines/consumer topic "gossip" {:zookeeper-address "localhost:2181"
                                      :schema-registry-url "http://localhost:8081"}))

(defn topic-whisperer [topic-stream ref path]
  (manifold/consume topic-stream (fn [event]
                                   (doseq [uid (get-in @ref path)]
                                     (chsk-send! uid [:msg event])))))

; TODO - refactor
(defn register [uid topic]
  (when-not (-> @shared-db :topics topic)
    (swap! shared-db #(assoc-in %1 [:topics topic] [])))
  (when-not (->> @shared-db
                 :topics
                 topic
                 (some #{uid}))
    (swap! shared-db
           #(update-in %1 [:topics topic] conj uid))))

(defmulti event-msg-handler :id)

(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (println (format "Unhandled event: %s %s" event ?data)))

(defmethod event-msg-handler :chsk/ws-ping [ev-msg]
  ; do something here?
  )

(defmethod event-msg-handler :topic/register
  [{:keys [uid ?data]}]
  (println "received registration request from " uid " to listen for " ?data)
                                        ; do something here?
  )

(defonce router (atom nil))
(defn stop-router! [] (when-let [stop-f @router] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router! ch-chsk event-msg-handler*)))

(start-router!)
