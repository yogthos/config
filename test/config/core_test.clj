(ns config.core-test
  (:require [clojure.test :refer :all]
            [config.core :as e]))

(defn refresh-ns []
  (ns-unalias *ns* 'e)
  (remove-ns 'edn-config.core)
  (dosync (alter @#'clojure.core/*loaded-libs* disj 'edn-config.core))
  (require '[config.core :as e]))

(defn refresh-env []
  (eval `(do (refresh-ns) e/env)))

(deftest test-env
  (testing "env variables"
    (is (= (:user e/env) (System/getenv "USER")))
    (is (= (:java-arch e/env) (System/getenv "JAVA_ARCH"))))
  (testing "system properties"
    (is (= (:user-name e/env) (System/getProperty "user.name")))
    (is (= (:user-country e/env) (System/getProperty "user.country"))))
  (testing "env file"
    (spit "test/config.edn" (prn-str {:foo "bar"}))
    (let [env (refresh-env)]
      (is (= (:foo env) "bar")))))
