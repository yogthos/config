(ns config.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as s]
            [clojure.tools.logging :as log])
  (:import java.io.PushbackReader))

;originally found in cprop https://github.com/tolitius/cprop/blob/6963f8e04fd093744555f990c93747e0e5889395/src/cprop/source.cljc#L26
(defn- str->value
  "ENV vars and system properties are strings. str->value will convert:
   the numbers to longs, the alphanumeric values to strings, and will use Clojure reader for the rest
   in case reader can't read OR it reads a symbol, the value will be returned as is (a string)"
  [v]
  (cond
    (re-matches #"[0-9]+" v) (Long/parseLong v)
    (re-matches #"^(true|false)$" v) (Boolean/parseBoolean v)
    (re-matches #"\w+" v) v
    :else
    (try
      (let [parsed (edn/read-string v)]
        (if (symbol? parsed) v parsed))
      (catch Throwable _ v))))

(defn- keywordize [s]
  (-> (s/lower-case s)
      (s/replace "_" "-")
      (s/replace "." "-")
      (keyword)))

(defn- sanitize-key [k]
  (let [s (keywordize (name k))]
    (if-not (= k s) (println "Warning: config key" k "has been corrected to" s))
    s))

(defn- read-system-env []
  (->> (System/getenv)
       (map (fn [[k v]] [(keywordize k) (str->value v)]))
       (into {})))

(defn- read-system-props []
  (->> (System/getProperties)
       (map (fn [[k v]] [(keywordize k) (str->value v)]))
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

(defn contains-in?
  "checks whether the nested key exists in a map"
  [m k-path]
  (let [one-before (get-in m (drop-last k-path))]
    (when (map? one-before)                        ;; in case k-path is "longer" than a map: {:a {:b {:c 42}}} => [:a :b :c :d]
      (contains? one-before (last k-path)))))

;; author of "deep-merge-with" is Chris Houser: https://github.com/clojure/clojure-contrib/commit/19613025d233b5f445b1dd3460c4128f39218741
(defn deep-merge-with
  "Like merge-with, but merges maps recursively, appling the given fn
  only when there's a non-map at a particular level.
  (deepmerge + {:a {:b {:c 1 :d {:x 1 :y 2}} :e 3} :f 4}
               {:a {:b {:c 2 :d {:z 9} :z 3} :e 100}})
  -> {:a {:b {:z 3, :c 3, :d {:z 9, :x 1, :y 2}}, :e 103}, :f 4}"
  [f & maps]
  (apply
    (fn m [& maps]
      (if (every? map? maps)
        (apply merge-with m maps)
        (apply f maps)))
    (remove nil? maps)))

(defn merge-maps [& m]
  (reduce #(deep-merge-with (fn [_ v] v) %1 %2) m))

(defn load-env
  "Generate a map of environment variables."
  [& configs]
  (let [env-props (merge-maps (read-system-env) (read-system-props))]
    (apply
      merge-maps
      (read-env-file ".lein-env")
      (read-env-file (io/resource ".boot-env"))
      (read-config-file "config.edn")
      (read-env-file (:config env-props))
      env-props
      configs)))

(defonce
  ^{:doc "A map of environment variables."}
  env (load-env))
