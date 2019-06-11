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

(deftest number-test
  (System/setProperty "bignum" "112473406068224456955")
  (is (= 112473406068224456955 (:bignum (e/load-env)))))

(deftest edn-test
  (let [props {"BOOL"          "true"
               "text"          "\"true\""
               "number"        "15"
               "quoted-number" "\"12\""
               "unparsed.text" "some text here"
               "edn_string"    "{:foo :bar :baz [1 2 \"foo\"]}"}]
    (doseq [[k v] props] (System/setProperty k v))
    (is
      (= {:bool          true,
          :text          "true",
          :number        15,
          :quoted-number "12",
          :edn-string    {:foo :bar, :baz [1 2 "foo"]},
          :unparsed-text "some text here"}
         (select-keys (e/load-env)
                      [:bool
                       :text
                       :number
                       :quoted-number
                       :edn-string
                       :unparsed-text])))))
