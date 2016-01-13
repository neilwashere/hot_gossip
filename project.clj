(defproject hot-gossip "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [reagent "0.5.1"]
                 [reagent-forms "0.5.11"]
                 [reagent-utils "0.1.5"]
                 [re-frame "0.6.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [environ "1.0.1"]
                 [http-kit "2.1.19"]
                 [com.taoensso/sente "1.7.0"]
                 [prone "1.0.0"]]

  :min-lein-version "2.5.3"

  :ring {:handler hot_gossip.handler/app
         :uberwar-name "hot_gossip.war"}

  :main hot-gossip.server

  :source-paths ["src/clj"]

  :uberjar-name "hot_gossip.jar"

  :plugins [[lein-environ "1.0.1"]
            [lein-asset-minifier "0.2.2"]]


  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]


  :minify-assets {:assets {"resources/public/css/site.min.css"
                           "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}


  :profiles {:dev {:repl-options {:init-ns hot-gossip.repl}

                   :dependencies [[ring/ring-mock "0.3.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [org.clojure/tools.nrepl "0.2.11"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]

                   :plugins [[lein-figwheel "0.5.0-2"]
                             [lein-cljsbuild "1.0.6"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :css-dirs ["resources/public/css"]
                              :ring-handler hot-gossip.handler/app}

                   :env {:dev true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "hot-gossip.dev"
                                                         :source-map true}}}}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :closure-defines {goog.DEBUG false}
                                               :pretty-print false}}}}}})
