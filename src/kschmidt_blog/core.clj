(ns kschmidt_blog.core
  (:require [stasis.core :as stasis]
            [markdown.core :as md]
            [clojure.string :as str]
            [clj-time.format :as tf]
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

(defn parse-datestring [date-str]
  (tf/parse (tf/formatter "yyyy-MM-dd") date-str))

(defn post-relative-url [post]
  (str (tf/unparse (tf/formatter "/yyyy/MM/dd/") (get-in post [:header :date]))
       (get-in post [:header :slug])))

(defn parse-post [post]
  (let [x (str/split post #"\n------\n")]
    {:body (second x)
     :header (let [header (read-string (first x))]
               (assoc header :date (parse-datestring (header :date))))}))

(defn post-absolute-url [uri post]
  (str uri (post-relative-url post)))
;; (map (partial post-absolute-url "www.kschmidt.com") (get-posts))

(defn filename [title date]
  (str (tf/unparse (tf/formatter "/yyyy/MM/dd/") date)
       title "/index.html"))

(defn post-filename [post]
  (filename (get-in post [:header :slug])
            (get-in post [:header :date])))

(def cegdown-ext [:fenced-code-blocks :autolinks])

(defn update-body [f post]
  (assoc post :body (f (post :body))))

(defn markdown [post]
  (update-body #(md2/to-html % cegdown-ext) post))

(def enliveify (partial update-body enlive/html-snippet))

(defn render [post]
  (update-body #(apply str (enlive/emit* %)) post))

(enlive/deftemplate index-post-template "partials/index_posts.html"
  [{:keys [header body] :as post}]
  [:span] (enlive/content (:title header))
  [:p] (enlive/content body)
  [:a#link] (enlive/set-attr :href (post-relative-url post))
  [:a#link] (enlive/set-attr :title (:title header)))

(enlive/deftemplate index-template "layouts/index.html" [posts]
  [:aside#author-bio] (enlive/html-content (slurp "resources/partials/author_bio.html"))
  [:footer#footer-content] (enlive/html-content (slurp "resources/partials/footer.html"))
  [:div.navigation] (enlive/html-content (slurp "resources/partials/navigation.html"))
  [:header] (enlive/html-content (slurp "resources/partials/header.html"))
  [:div#articles] (enlive/html-content (apply str (map #(apply str (index-post-template %)) posts))))

(defn apply-post-layout [post]
  (assoc post :body (apply str (index-post-template post))))

(defn get-posts []
  (->> (stasis/slurp-directory "resources/articles/" #".*\.(md|markdown)$")
       (vals)
       (map (comp apply-post-layout
                  render
                  enliveify
                  markdown
                  parse-post))))

(defn get-index [posts]
  (into {} [["/index.html" (apply str (index-template posts))]]))

;;(defn handler [request]
;;  {:status 200
;;   :headers {"Content-Type" "text/html"}
;;   :body "Hello World"})

;;(def app
;;  (stasis/serve-pages (markdown-pages posts)))


(def app
  (stasis/serve-pages (get-index (get-posts))))
