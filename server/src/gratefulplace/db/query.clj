(ns gratefulplace.db.query
  (:require [datomic.api :as d])
  (:use environ.core))

(def ^:dynamic *db-uri* (:db-uri (env :datomic)))
(defn conn
  []
  (d/connect *db-uri*))
(defn db
  []
  (d/db (conn)))

;; '[:find ?c :where [?c :topic/title]]
(def q
  #(d/q % (db)))

(defn ent
  [id]
  (if-let [exists (ffirst (d/q '[:find ?eid :in $ ?eid :where [?eid]] (db) id))]
    (d/entity (db) exists)
    nil))

(defn eid
  [& conditions]
  (let [conditions (map #(concat ['?c] %) conditions)]
    (-> {:find ['?c]
         :where conditions}
        q
        ffirst)))

(defn one
  [& conditions]
  (if-let [id (apply eid conditions)]
    (ent id)))

(defn all
  [common-attribute & conditions]
  (let [conditions (concat [['?c common-attribute]]
                           (map #(concat ['?c] %) conditions))]
    (map #(ent (first %)) (q {:find ['?c]
                              :where conditions}))))

(def t
  #(d/transact (conn) %))

(defn resolve-tempid
  [tempids tempid]
  (d/resolve-tempid (db) tempids tempid))