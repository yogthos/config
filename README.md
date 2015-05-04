## `luminus/config`


A library for managing configuration using environment variables and EDN configuration files.

The configuration is resolved in the following order, the variables found in later configurations will replaces those declared earlier:

1. `config.edn` on the classpath
2.  `.lein-env` file in the project directory
3. Environment variables
4. Java system properties

## Installation

Include the following dependency in your project.clj file:

[![Clojars Project](https://img.shields.io/clojars/v/luminus/config.svg)](https://clojars.org/luminus/config)

## Usage

The library will look for the `config.edn` file on the classpath. The contents of this
file will be merged with the environment variables found in `System/getenv` and `System/getProperties`.

Setting up multiple configurations is done by adding profiles to `project.clj`. For example,
if we wanted to have a `dev` and a `prod` config we would follow the following steps.

First, we create a `config` folder in the root of the project. Under the `config` we will create `dev`
and `prod` folders. Each of this will contain a file called `config.edn`.

The configuration might look as follows:

```clojure
{:database-url "jdbc:postgres://localhost/dev"}
``` 

Next, we will add the dependency and the profiles to our `project.clj`:

```clojure
(defproject edn-config-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [luminus/config "0.1"]]
  :profiles {:prod {:resource-paths ["config/prod"]}
             :dev  {:resource-paths ["config/dev"]}}
  :main edn-config-test.core)

```

We can now access the config variables the `config.edn` found under the resource path specified in the profile.


```clojure
(ns edn-config-test.core
  (:require [edn-config.core :refer [env]])
  (:gen-class))

(defn -main []
  (println (:dev env) (:db env)))
```

The application can be packaged using a specific profile by using the Leiningen `with-profile` option.
For example, if we wanted to package with the `prod` profile then we'd run the following:

```
lein with-profile prod uberjar
```

The resulting `jar` will contain the config found in `config/prod/config.edn`.

### Attributions

The `luminus/config` project uses [environ](https://github.com/weavejester/environ) for managing environment variables.

## License

Distributed under the Eclipse Public License, the same as Clojure.
