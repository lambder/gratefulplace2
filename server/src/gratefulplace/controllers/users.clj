(ns gratefulplace.controllers.users
  (:require [gratefulplace.db.validations :as validations]
            [gratefulplace.db.serialize :as s]
            [gratefulplace.db.serializers :as ss]
            [gratefulplace.db.query :as q]
            [cemerick.friend :as friend]
            cemerick.friend.workflows)
  (:use [flyingmachine.webutils.validation :only (if-valid)]))

(defn registration-success-response
  [req]
  (if (friend/current-authentication)
    {:body "success"}))

(defn attempt-registration
  [req]
    (let [{:keys [uri request-method params session]} req]
    (when (and (= uri "/users")
               (= request-method :post))
      (if-valid
       params
       (:create validations/user)
       errors

       (do
         (q/t [(s/serialize params ss/user->txdata)])
         (cemerick.friend.workflows/make-auth
          (select-keys params [:username])
          {:cemerick.friend/redirect-on-auth? false}))
       {:body {:errors errors}
        :status 412}))))