(ns hot-gossip.ws
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [manifold.stream :as s]
            [tubelines.core :as tl]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {:user-id-fn (fn [ring-req] "hard-code-for-test")})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

(defonce shared-db (atom {:topics {}}))

(def kafka-config {:zookeeper-address "localhost:2181"
                   :schema-registry-url "http://localhost:8081"})

(defn broadcast
  "Broadcast data to all connected clients"
  [data]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid data)))

(defn topic-stream [topic]
  (tl/consumer topic "gossip" kafka-config))

(defn topic-whisperer [topic-stream ref path]
  (s/consume (fn [event]
               (doseq [uid (get-in @ref path)]
                 (chsk-send! uid [:topic/event event])))
             topic-stream))

(defn publish-topic [?data]
  (with-open [p (tl/producer (:topic ?data) kafka-config)]
    (tl/publish! p (:data ?data))))

(defn fake-stream [topic]
  (let [stream (s/stream)]
    (future
      (while (not (s/closed? stream))
        (s/put! stream {:topic topic
                        :data "hello"})
        (Thread/sleep 1000)))
    stream))

(defn fake-publish [{:keys [topic data]}]
  (if-let [s (:topic topic :stream)]
    (s/put! s data)))

; TODO - refactor OMG
(defn register-topic-watcher [uid topic]
  (when-not (-> @shared-db :topics topic)
    (let [stream (topic-stream topic)
          processor (topic-whisperer stream shared-db [:topics topic :uids])]

      (swap! shared-db #(assoc-in %1 [:topics topic] {:stream stream
                                                      :uids [uid]
                                                      :processor processor}))))

  (when-not (->> @shared-db
                 :topics
                 topic
                 :uids
                 (some #{uid}))
    (swap! shared-db
           #(update-in %1 [:topics topic :uids] conj uid))))

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
  (register-topic-watcher uid ?data))

(defmethod event-msg-handler :topic/post
  [{:keys [uid ?data]}]
  (println uid " sending msg to topic " ?data)
  (publish-topic ?data))

(defonce router (atom nil))
(defn stop-router! [] (when-let [stop-f @router] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router! ch-chsk event-msg-handler*)))

(start-router!)
