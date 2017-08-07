(ns config.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [clojure.tools.logging :as log])
  (:import java.io.PushbackReader))

(defn- keywordize [s]
  (-> (s/lower-case s)
      (s/replace "_" "-")
      (s/replace "." "-")
      (keyword)))

(defn- sanitize-key [k]
  (let [s (keywordize (name k))]
    (if-not (= k s) (println "Warning: environ key" k "has been corrected to" s))
    s))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-system-props []
  (->> (System/getProperties)
       (map (fn [[k v]] [(keywordize k) v]))
       (into {})))

(defn- read-env-file [f]
  (try
    (when-let [env-file (io/file f)]
      (when (.exists env-file)
        (into {} (for [[k v] (edn/read-string (slurp env-file))]
                   [(sanitize-key k) v]))))
    (catch Exception e
      (log/warn (str "WARNING: failed to parse " f " " (.getLocalizedMessage e))))))

(defn- read-config-file [f]
  (try
    (when-let [url (io/resource f)]
      (with-open [r (-> url io/reader PushbackReader.)]
        (edn/read r)))
    (catch Exception e
      (log/warn (str "WARNING: failed to parse " f " " (.getLocalizedMessage e))))))

(defonce ^{:doc "A map of environment variables."}
  env
  (let [env-props (merge (read-system-env) (read-system-props))]
    (merge env-props
           (read-env-file (:config env-props))
           (read-env-file ".lein-env")
           (read-env-file (io/resource ".boot-env"))
           (read-config-file "config.edn"))))

