(ns hot-gossip.server
  (:require [hot-gossip.handler :refer [app]]
            [hot-gossip.ws :refer [start-router!]]
            [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (run-server app {:port port :join? false})))
