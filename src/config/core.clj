(ns config.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as s])
  (:import java.io.PushbackReader))

(defn- keywordize [s]
  (-> (s/lower-case s)
      (s/replace "_" "-")
      (s/replace "." "-")
      (keyword)))

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
                   [(keywordize k) v]))))
    (catch Exception e
      (println (str "WARNING: failed to parse " f " " (.getLocalizedMessage e))))))

(defn- read-config-file [f]
  (try
    (when-let [url (io/resource f)]
      (with-open [r (-> url io/reader PushbackReader.)]
        (edn/read r)))
    (catch Exception e
      (println (str "WARNING: failed to parse " f " " (.getLocalizedMessage e))))))

(defonce ^{:doc "A map of environment variables."}
  env
  (let [env-props (merge (read-system-env) (read-system-props))]
    (merge
      (read-config-file "config.edn")
      (read-env-file (:config env-props))
      (read-env-file ".lein-env")
      (read-env-file (io/resource ".boot-env"))
      env-props)))

