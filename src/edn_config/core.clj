(ns edn-config.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [environ.core :as environ])
  (:import java.io.PushbackReader))

(defn- read-config-file []
  (try
    (with-open [r (-> "config.edn" io/resource io/reader PushbackReader.)]
    (edn/read r))
    (catch Exception _)))

(defonce env (merge (read-config-file) environ/env))
