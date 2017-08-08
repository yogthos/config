(ns config.core-test
  (:require [clojure.test :refer :all]
            [config.core :as e]))

(deftest test-env
  (testing "env variables"
    (is (= (System/getenv "USER") (:user (e/load-env))))
    (is (= (System/getenv "JAVA_ARCH") (:java-arch (e/load-env)))))
  (testing "system properties"
    (is (= (System/getProperty "user.name") (:user-name (e/load-env))))
    (is (= (System/getProperty "user.country") (:user-country (e/load-env)))))
  (testing "env file"
    (spit "test/config.edn" (prn-str {:foo "bar"}))
    (is (= "bar" (:foo (e/load-env)))))
  (testing "custom config"
    (is (= "custom prop" (:custom-prop (e/load-env {:custom-prop "custom prop"}))))))
