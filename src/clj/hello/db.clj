(ns hello.db
  (:require [integrant.core :as ig]
            [crux.api :as crux]))


(defmethod ig/init-key ::node [_ config]
  (crux/start-node config))


(defmethod ig/halt-key! ::node [_ node]
  (when node
    (.close ^java.io.Closeable node)))
