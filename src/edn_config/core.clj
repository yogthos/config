(ns edn-config.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import java.io.PushbackReader))

(defn- keywordize [s]
  (-> (str/lower-case s)
      (str/replace "_" "-")
      (str/replace "." "-")
      (keyword)))

(defn- sanitize [k]
  (let [s (keywordize (name k))]
    (if-not (= k s) (println "Warning: edn-config key " k " has been corrected to " s))
    s))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-system-props []
  (->> (System/getProperties)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-config-file []
  (try
    (with-open [r (-> "config.edn" io/resource io/reader PushbackReader.)]
    (edn/read r))
    (catch Exception _)))

(defonce ^{:doc "A map of environment variables."}
  env
  (merge
   (read-config-file)
   (read-system-env)
   (read-system-props)))
