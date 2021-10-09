(ns hello.config
  (:require [clojure.java.io :as io]
            [aero.core :as aero]))


#_{:clj-kondo/ignore [:unused-binding]}
(defmethod aero/reader 'file [opts tag value]
  (io/file value))


(defn load-config []
  (let [config (-> "config.edn"
                   (clojure.java.io/resource)
                   (aero/read-config))
        mode   (-> config :mode)]
    (when-not (#{:dev :prod} mode)
      (throw (ex-info (str "Illegal mode: " (pr-str mode)) {:mode mode})))
    config))


(comment
  (./aprint (load-config))
  )
