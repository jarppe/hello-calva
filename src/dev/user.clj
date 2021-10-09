(ns user
  (:require [clojure.tools.logging :as log]
            [integrant.repl :as igr]
            [integrant.repl.state :as state]
            [kaocha.repl :as k]
            [lucid.core.inject :as inject]))


;;
;; Programming utils:
;;


(inject/inject '[.
                 [aprint.core aprint]
                 [clojure.repl doc source dir]
                 [lucid.core.debug dbg-> dbg->> ->prn ->doto ->>doto]])


;;
;; Integrant repl setup:
;;


(igr/set-prep!
  (fn []
    (require 'user)
    ((requiring-resolve 'hello.components/init-components))))


(def start igr/init)
(def stop igr/halt)
(def reset igr/reset)
(defn system [] state/system)


(defn run-unit-tests []
  (k/run :unit))


(defn run-all-tests []
  (run-unit-tests))


(log/info "user ns loaded")


(defn node [] (-> (system) :hello.db/node))


(comment
  (require '[crux.api :as crux])
  (require '[clj-uuid :as uuid])

  (let [op   (uuid/v1)
        node (node)
        tx   (crux/submit-tx node
                             [[:crux.tx/put {:crux.db/id    op
                                             :hello/op-user "123"}]
                              [:crux.tx/put {:crux.db/id       "jarppe"
                                             :hello/op         op
                                             :hello/first-name "Jarppe"
                                             :hello/last-name  "Länsiö"
                                             :hello/email      "jarppe.lansio@metosin.fi"
                                             :hello/age        52}]])]
    (println "TX:" tx)
    (crux/await-tx node tx)
    (println "Committed" (crux/tx-committed? node tx)))


  (let [node (node)
        db   (crux/db node)
        r    (crux/q db
                     '{:find  [(pull ?e [*])]
                       :where [[?e :hello/email email]]
                       :in    [email]}
                     "jarppe.lansio@metosin.fi")]
    (-> r ffirst ./aprint))

  (-> (node)
      (crux/db)
      (crux/q '{:find  [?e ?age]
                :where [[?e :hello/email email]
                        [?e :hello/age ?age]]
                :in    [email]}
              "jarppe.lansio@metosin.fi")
      (./aprint))

  (-> (node) (crux/db) (crux/pull '[*] "jarppe"))
  (-> (node) (crux/db) (crux/entity "jarppe"))

  (let [node (node)
        db   (crux/db node)]
    (with-open [h (crux/open-entity-history db "jarppe" :asc {:with-docs? true})]
      (./aprint (iterator-seq h))))

  (defn assert-email-is-unique [ctx email]
    (-> ctx
        (crux/db)
        (crux/q '{:find  [?e]
                  :where [[?e :hello/email email]]
                  :in    [email]}
                email)
        (ffirst)
        (if false [])))


  (defmacro deftxfn [tx-fn-name args & form]
    (let [get-ns (fn [e]
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
      `(defn ~tx-fn-name {:txfn? true} ~args ~@form*)))

  (macroexpand-1 '(deftxfn fofo [a b]
                           (-> ctx
                             (crux/db)
                             (crux/q {:find  [?e]
                                      :where [[?e :hello/email email]]
                                      :in    [email]}
                                     email)
                             (ffirst)
                             (if false []))))


  (deftxfn fofo [a b]
           (-> ctx
               (crux/db)
               (crux/q {:find  [?e]
                        :where [[?e :hello/email email]]
                        :in    [email]}
                       email)
               (ffirst)
               (if false [])))

  (name 'crux/db)
  (let [e 'crux/db]
    (-> e
        (namespace)
        (symbol)
        ((ns-aliases *ns*))
        (ns-name)
        (name)))

  (symbol (namespace 'crux/db) (name 'crux/db))
  (ns-name ((ns-aliases *ns*) 'crux))
  (ns-name nil)

  (assert-email-is-unique (node) "jarppe.lansio@metosin.fi")

  (let [node (node)
        tx   (crux/submit-tx node [[:crux.tx/put {:crux.db/id :assert-email-is-unique
                                                  :crux.db/fn '(fn [ctx email]
                                                                 (-> ctx
                                                                     (crux.api/db)
                                                                     (crux.api/q '{:find  [?e]
                                                                                   :where [[?e :hello/email ?email]]
                                                                                   :in    [?email]}
                                                                                 email)
                                                                     (ffirst)
                                                                     (if false [])))}]])]
    (crux/await-tx node tx)
    (println "Committed" (crux/tx-committed? node tx)))

  (let [op    (uuid/v1)
        id    (uuid/v1)
        email "jarppe.lansio@metosin.fizz"
        db    (-> db/node
                  (crux/db)
                  (crux/with-tx [[:crux.tx/put {:crux.db/id    op
                                                :hello/op-user "123"}]
                                 [:crux.tx/fn :assert-email-is-unique email]
                                 [:crux.tx/put {:crux.db/id       id
                                                :hello/op         op
                                                :hello/first-name "Jarppe"
                                                :hello/last-name  "Länsiö"
                                                :hello/email      email
                                                :hello/age        52}]]))]
    (if (not db)
      (println "Oh no, tx failed")
      (./aprint (crux/q db '{:find  [(pull ?e [*])]
                             :where [[?e :hello/email]]}))))

  (require '[aero.core :as aero])
  (-> "config.edn" (clojure.java.io/resource) (aero/read-config))
  )
