(defproject kschmidt-blog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [stasis "2.3.0"]
                 [markdown-clj "0.9.85"]
                 [hiccup "1.0.5"]
                 [me.raynes/cegdown "0.1.1"]
                 [clygments "0.1.1"]
                 [enlive "1.1.6"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler kschmidt_blog.core/app})
