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

(defonce shared-db (atom {}))

(defmulti event-msg-handler :id)

(defn event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
  (event-msg-handler ev-msg))

(defmethod event-msg-handler :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (println (format "Unhandled event: %s %s" event ?data)))

(defmethod event-msg-handler :chsk/ws-ping [ev-msg]
  ; do something here?
  )

(defmethod event-msg-handler :kafka/watch
  ; Clients register an interest in receiving events from a topic
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (println "received a watch request here " ev-msg)
  ; TODO - add the logic to register a new topic or listen to an existing one
  )


(defonce router (atom nil))
(defn stop-router! [] (when-let [stop-f @router] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router! ch-chsk event-msg-handler*)))

(start-router!)
