(ns hello.components
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]
            [integrant.core :as ig]
            [hello.config :as config])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))


(defn components [config]
  {;; Crux
   :hello.db/node (-> config :crux)
   })


(defn init-components []
  (System/setProperty "org.jboss.logging.provider" "slf4j")
  (SLF4JBridgeHandler/install)
  (let [config (config/load-config)]
    (log/info "initializing components with mode" (-> config :mode (name) (str/upper-case)))
    (components config)))


(defn init-system []
  (try
    (-> (init-components)
        (ig/init))
    (log/info "server running")
    (catch Throwable e
      (.println System/err "unexpected error while starting server")
      (.printStackTrace e System/err)
      (System/exit 1))))
