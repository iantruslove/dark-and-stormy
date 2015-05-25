(defproject dark-and-stormy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["etc" "resources"]
  :main dark-and-stormy.core
  :dependencies [[cider/cider-nrepl "0.8.2"]
                 [clj-http "1.1.2"]
                 [cheshire "5.4.0"]
                 [com.stormpath.sdk/stormpath-sdk-api "1.0.RC4.2"]
                 [com.stormpath.sdk/stormpath-sdk-httpclient "1.0.RC4.2"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [com.stuartsierra/component "0.2.3"]
                 [compojure "1.3.4"]
                 [doric "0.9.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [ring "1.4.0-RC1"]
                 [sonian/carica "1.1.0"]]
  :profiles {:dev {:global-vars {clojure.core/*warn-on-reflection* true}
                   :plugins [[lein-deps-tree "0.1.2"]]}})
