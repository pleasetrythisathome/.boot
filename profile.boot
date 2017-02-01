(set-env! :dependencies (fn [deps]
                          (->> '[[boot-deps "0.1.6"]
                                 ;;[im.chit/vinyasa "0.4.2" :exclusions [org.clojure/clojure]]
                                 [org.jsoup/jsoup "1.7.1"]
                                 [io.aviso/pretty "0.1.30"
                                  :exclusions [org.clojure/clojure]]]
                               (mapv #(conj % :scope "test"))
                               (concat deps)
                               vec)))

(require '[boot-deps :refer [ancient]])
(require '[io.aviso.repl :refer [install-pretty-exceptions]])
;;(require '[vinyasa.inject :as inject])

(install-pretty-exceptions)

(deftask cider
  "CIDER profile"
  []
  (require 'boot.repl)
  (swap! @(resolve 'boot.repl/*default-dependencies*)
         concat '[[org.clojure/tools.nrepl "0.2.12"]
                  [org.clojure/tools.namespace "0.3.0-alpha3"]
                  [cider/cider-nrepl "0.15.0-SNAPSHOT"
                   :exclusions [org.clojure/tools.reader
                                org.clojure/java.classpath]]
                  [com.cemerick/piggieback "0.2.1"
                   :exclusions [org.clojure/clojure
                                org.clojure/clojurescript]]
                  [refactor-nrepl "2.3.0-SNAPSHOT"
                   :exclusions [org.clojure/tools.nrepl]]])
  (swap! @(resolve 'boot.repl/*default-middleware*)
         concat '[cider.nrepl/cider-middleware
                  cemerick.piggieback/wrap-cljs-repl
                  refactor-nrepl.middleware/wrap-refactor])
  identity)

#_(inject/in [vinyasa.inject :refer [inject [in inject-in]]]
             clojure.core
             [vinyasa.reflection .> .? .* .% .%> .& .>ns .>var]
             ;; inject into clojure.core with prefix
             clojure.core >
             [clojure.pprint pprint]
             [clojure.java.shell sh])

#_(defn- generate-lein-project-file!
  [& {:keys [keep-project] :or {:keep-project true}}]
  (require 'clojure.java.io)
  (let [pfile ((resolve 'clojure.java.io/file) "project.clj")
                                        ; Only works when pom options are set using task-options!
        {:keys [project version]} (:task-options (meta #'boot.task.built-in/pom))
        prop #(when-let [x (get-env %2)] [%1 x])
        head (list* 'defproject (or project 'boot-project) (or version "0.0.0-SNAPSHOT")
                    (concat
                     (prop :url :url)
                     (prop :license :license)
                     (prop :description :description)
                     [:dependencies (get-env :dependencies)
                      :source-paths (vec (concat (get-env :source-paths)
                                                 (get-env :resource-paths)))]))
        proj (pp-str head)]
    (if-not keep-project (.deleteOnExit pfile))
    (spit pfile proj)))

#_(deftask lein-generate
  "Generate a leiningen `project.clj` file.
   This task generates a leiningen `project.clj` file based on the boot
   environment configuration, including project name and version (generated
   if not present), dependencies, and source paths. Additional keys may be added
   to the generated `project.clj` file by specifying a `:lein` key in the boot
   environment whose value is a map of keys-value pairs to add to `project.clj`."
  []
  (generate-lein-project-file! :keep-project true))
