(ns hello.basics-test
  (:require [clojure.test :refer :all]
            [crux.api :as crux]
            [hello.db :as db]
            [hello.test-node]))


(use-fixtures :once hello.test-node/with-test-node)

#_(deftest submit-tx
    (let [tx (crux/submit-tx db/node [[:crux.tx/put {:crux.db/id   :test
                                                     :test/message "hullo"}]])
          _  (crux/await-tx db/node tx)
          e  (crux/entity (crux/db db/node) :test)]
      (is (= e {:crux.db/id   :test
                :test/message "hullo"}))))

#_(deftest query
    (let [tx (crux/submit-tx db/node [[:crux.tx/put {:crux.db/id   :test1
                                                     :test/kind    :message
                                                     :test/message "hullo"}]
                                      [:crux.tx/put {:crux.db/id   :test2
                                                     :test/kind    :message
                                                     :test/message "howdy"}]])
          _  (crux/await-tx db/node tx)
          r  (crux/q (crux/db db/node) '{:find  [m]
                                         :where [[e :test/kind :message]
                                                 [e :test/message m]]})]
      (is (= r #{["hullo"] ["howdy"]}))))

#_
(defmacro deftxfn [tx-fn-name args & form]
  (let [get-ns  (fn [e]
                  (if (qualified-symbol? e)
                    (namespace e)))
        aliases (ns-aliases *ns*)
        form*   (clojure.walk/postwalk (fn [e]
                                         (or (some-> e
                                                     (get-ns)
                                                     (symbol)
                                                     (aliases)
                                                     (ns-name)
                                                     (name)
                                                     (symbol (name e)))
                                             e))
                                       form)]
    `(defn ~tx-fn-name {:txfn '(fn ~args ~@form*)} ~args ~@form*)))

#_
(deftxfn fofo [ctx email]
  (println "here we go")
  (-> ctx
      (crux/db)
      (crux/q '{:find  [?e]
                :where [[?e :hello/email email]]
                :in    [email]}
              email)
      (ffirst)
      (if false [])))

#_
(-> #'fofo meta :txfn)

#_
(fofo (user/node) "jarppe.lansio@metosin.fi")
