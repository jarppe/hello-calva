(ns hello.main
  (:require [hello.components :as components]))


(defn -main [& _]
  (components/init-system))
