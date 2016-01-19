(ns kschmidt_blog.core
  (:require [stasis.core :as stasis]
            [markdown.core :as md]
            [clojure.string :as str]
            [hiccup.page :refer [html5]]
            [clojure.java.io :as io]
            [me.raynes.cegdown :as md2]
            [clygments.core :as pygments]
            [net.cgrand.enlive-html :as enlive]))

(def pages {"/index.html" "<h1>Welcome!</h1>"})
(def posts (stasis/slurp-directory "resources/articles/" #"\.md$"))
(def pegdown-options
  [:autolinks :fenced-code-blocks :strikethrough])

(defn- extract-code
  [highlighted]
  (-> highlighted
      java.io.StringReader.
      enlive/html-resource
      (enlive/select [:pre])
      first
      :content))

(defn- highlight [node]
  (let [code (->> node :content (apply str))
        lang (->> node :attrs :class keyword)]
    (assoc node :content (-> code
                             (pygments/highlight lang :html)
                             extract-code))))

(defn highlight-code-blocks [page]
  (enlive/sniptest page
                   [:pre :code] highlight
                   [:pre :code] #(assoc-in % [:attrs :class] "codehilite")))

;;(defn prepare-pages [pages]
;;  (zipmap (keys pages)
;;          (map #(highlight-code-blocks %) (vals pages))))

(defn layout-page [page]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:title "Tech blog"]
    [:link {:rel "stylesheet" :href "/pygments-css/autume.css"}]]
   [:body
    [:div.logo "kschmidt.com"]
    [:div.body page]]))

(defn about-page [request]
  (layout-page (slurp (io/resource "partials/about.html"))))

(defn partial-pages [pages]
  (zipmap (keys pages)
          (map layout-page (vals pages))))

(defn update-body [f post]
  (assoc post :body (f (post :body))))

(defn markdown [post]
  (md2/to-html post))

(defn render-markdown-page [page]
  (layout-page (md2/to-html page pegdown-options)))

(defn markdown-pages [pages]
  (zipmap (map #(str/replace % #"\.md$" "/") (keys pages))
          (map render-markdown-page (vals pages))))

(defn get-posts []
  (->> (stasis/slurp-directory "resources/articles/" #"\.md$")
       (vals)
       (markdown)))

(defn get-posts2 []
  (doseq [v (vals (stasis/slurp-directory "resources/articles/" #"\.md$"))]
    (markdown v)))

(defn get-posts3 [map func]
  (into {} (for [[k v] map] [k (func v)])))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

;;(defn markdown-pages [pages]
;;  (zipmap (map #(str/replace % #"\.md$" "/") (keys pages))
;;          (map #(layout-page (md/md-to-html-string %)) (vals pages))))

;;(defn get-pages []
;;  (stasis/merge-page-sources
;;   {:public (stasis/slurp-directory "resources/public" #".*\.(html|css|js)$")
;;    :partials (partial-pages (stasis/slurp-directory "resources/partials" #".*\.html$"))
;;    :markdown
;;    (markdown-pages (stasis/slurp-directory "resources/articles" #"\.md$"))}))

(defn get-raw-pages []
  (stasis/merge-page-sources
   {:public
    (stasis/slurp-directory "resources/public" #".*\.(html|css|js)$")
    :partials
    (partial-pages (stasis/slurp-directory "resources/partials" #".*\.html$"))
    :markdown
    (markdown-pages (stasis/slurp-directory "resources/articles" #"\.md$"))}))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(fn [req] (highlight-code-blocks %)) (vals pages))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; ACTUAL
;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;(defn get-pages []
;;  (prepare-pages (get-raw-pages)))

(def posts2 (array-map "Star Wars" "2016-01-11" "Star Trek" "2016-01-18"))
(def posts3 (array-map "Star Wars" "2016-01-11"))

(defn post-relative-url [post]
  (str (tf/unparse (tf/formatter "/yyyy/MM/dd/") (get-in post [:header :date]))
       (get-in post [:header :slug])
       "/"))

(defn post-absolute-url [uri post]
  (str uri (post-relative-url post)))

(enlive/deftemplate index-post-template "partials/index_posts.html"
  [post]
  [:span] (enlive/content (first (keys post)))
  [:p] (enlive/content (first (vals post)))
;;  [:a#link] (enlive/set-attr :href (p/post-relative-url post))
  [:a#link] (enlive/set-attr :title (first (keys post))))

(enlive/deftemplate index-template "layouts/index.html" []
  [:aside#author-bio] (enlive/html-content (slurp "resources/partials/author_bio.html"))
  [:footer#footer-content] (enlive/html-content (slurp "resources/partials/footer.html"))
  [:div.navigation] (enlive/html-content (slurp "resources/partials/navigation.html"))
  [:header] (enlive/html-content (slurp "resources/partials/header.html")))
;;  [:div#articles] (enlive/html-content (apply str (index-post-template))))

(defn get-index []
  (into {} [["/index.html" (apply str (index-template))]]))

;;(defn handler [request]
;;  {:status 200
;;   :headers {"Content-Type" "text/html"}
;;   :body "Hello World"})

;;(def app
;;  (stasis/serve-pages (markdown-pages posts)))


(def app
  (stasis/serve-pages (get-index)))
