(ns hello.test-node
  (:require [clojure.test :refer :all]
            [crux.api :as crux]
            [hello.db :as db]))


(defn with-test-node [f]
  #_(with-open [test-node (crux/start-node {})]
    (with-redefs [db/node test-node]
      (f))))
