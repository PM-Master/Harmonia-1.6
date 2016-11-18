(ns toolboxes.cljtoolbox)
(import '(java.io File)
        '(javax.swing JFileChooser)
        '(javax.swing.filechooser FileFilter)
	      '(javax.swing BoxLayout)
        '(java.util UUID)
        '(java.awt.event ActionListener))


(defn uuid
  ([size] (-> (uuid) (.substring 0 size)))
  ([] (-> (UUID/randomUUID) (.toString) (.toUpperCase) (.replace "-" ""))))

(defn joins
  ([coll]
    (joins coll ""))
  ([coll sep]
    (apply str (interpose sep coll))))

(defn sjoin [sep coll] (joins coll sep))

(defn split ([s] (split "\n" s))
  ([sep s] (.split (str s) sep)))


(defn sprop
  "Shortcut for gettting a system property"
  [key] (System/getProperty key))

(defn listener
  "Takes a method with a single argument and wraps it with an ActionListener proxy "
  [action_listener_method] (proxy [ActionListener] [] (actionPerformed [ae] (action_listener_method ae))))


(def notification-subscriptions (atom {}))

(defrecord NotificationSubscription [subscriber id handler context])

(defn post-notification
  "
post a notification

A notification is comprised of
1.  An identifier keyed :id
2.  A sender keyed :sender
"
  [id sender]
  (println "Posting the notification" id "from" sender))

(defn add-subscription
  "adds the subscription by pulling the subscriptions atom and updating its contents"
  [sub]
  (let [new-subs (if-let [subs (get @notification-subscriptions (:id sub))]
		   (cons sub subs)
		   (cons sub '()))]
    (swap! notification-subscriptions assoc (:id sub) new-subs)))

(defn subscribe-to-notification
  "
Subscribe to a notification

1.  subscriber - the subscribing object.  An object can subscribe to a given notification only once.
1.  id - an identifier.  Not specifying an id will subscribe to all notifications (not advised)
2.  handler a handling function.  This function must take three params [id, sender, context].  Any return value will be ignored.
3.  context - unknown (optional).  This can be whatever you would like passed to the handling function's context param.  If unspecified context will be nil.
"
  [subscriber id handler context]
  (let [sub (NotificationSubscription. subscriber id handler context)]
    (add-subscription sub)))

(defn get-subscription-ids []
  (map #(first %1) @notification-subscriptions))

(defn unsubscribe-to-notification [id subscriber]
  (if-let [subs (get @notification-subscriptions id)]
    (let [new-subs (remove #(= (:subscriber %1) subscriber) subs)]
      (swap! notification-subscriptions assoc id new-subs))))

(defn add-notification-poster [comp id]
  (.addActionListener comp (listener (fn [actionEvent] (post-notification id comp)))))

(defn get-method-names
  "Gets a list of method names as a sequence of strings"
  [obj] (distinct (map #(.. %1 getName toString)(seq (.. obj getClass getMethods)))))

(defn print-methods [obj] (apply print (interpose "\n" (sort (get-method-names obj)))))

(defn get-param-types [method] (interpose " " (map #(.getName %1) (.getParameterTypes method))))

(defn print-method-signatures [obj]
  (apply print
	 (interpose "\n"
		    (map #(apply str "Method "
			       (.getReturnType %1) " "
			       (.getName %1) " "
			       (get-param-types %1))
			 (sort-by #(.getName %1) (.. obj getClass getMethods))))))

(defn classes-congruent [classes_declared classes_given]
  (or
    (and (empty? classes_declared)
         (empty? classes_given))
    (if (= (count classes_declared) (count classes_given))
      (loop [classes_decl classes_declared,
             classes_give classes_given]
        (println "Decl" classes_decl)
        (println "Given" classes_give)
          (or (empty? classes_decl)
            (and
              (let [result (clojure.lang.Reflector/paramArgTypeMatch (first classes_decl) (first classes_give))] (println "Result" result) result)
              (recur (rest classes_decl) (rest classes_give))))

        )
      false)))

(defn responds-to?

  "obj - object you would like to test the response of
   method_name - name of the method you would like to test for the presence of
   args - types of the arguments as they are specified in the method"

  [obj method_name args]
  (let [cls (class obj)
        methods (clojure.lang.Reflector/getMethods cls (count args) method_name true)
        ]

        (every? #((classes-congruent (.getParameterTypes %1) args)) methods)
    )

  )


(def any? (comp not nil? some))

(defn obj-props [obj]
  (let [getters (filter #(.. %1 getName (startsWith "get")) (seq (.. obj getClass getMethods)))]
       (map #(. %1 invoke obj nil) getters)
    )
  )

(import 'javax.swing.JFrame)
(def *line-in* (java.io.BufferedReader. *in*))

(defn ask-user [question]
  (print question " " )
  (let [answer (.readLine *line-in*)]
    answer))

(defn quit [] (System/exit 0))

(defn get-active-window []
  (if-let [active-windows (->> (JFrame/getWindows) (filter #(.isActive %)))]
    (first active-windows)
    nil))

(defn close-all-windows
  ([] (doseq [window (JFrame/getWindows)] (.dispose window)))
  ([ask_first]
     (if ask_first
       (print "Would you like to close all open windows")
       (let [answer (.toUpperCase (ask-user))]
	 (if-not (.startsWith answer "N")
	   (close-all-windows))))))

(defn awt-dim [width height] (java.awt.Dimension. height width))

(defn remove-all-action-listeners
  "A handy method"
  ([comp]
  (if
    (and
      (responds-to? comp "getActionListeners" [])
      (responds-to? comp "removeActionListener" [java.awt.event.ActionListener]))
      (
        (let [al (.getActionListeners comp)]
           (dotimes [n (count al)]
              (println "removing" n)
              (.removeActionListener comp (nth al n))
              )
          )
      )))
  ([comp & more]
    (
      (remove-all-action-listeners comp)
      (apply remove-all-action-listeners more))))


					;Clojure swing generation methods
(defmulti gen-default
  (fn [propkey, properties]
    (print "method dispatch for" propkey properties)
    (if (not (nil? (get properties propkey)))
      :default
      propkey)))

(defmethod gen-default :id [propkey, properties]
	   (merge properties {:id (str (int (rand 1000000))  "-id")}))

(defmethod gen-default :notification [propkey, properties]
	   (let [props-with-id (gen-default :id properties)]
	     (merge
	      {:notification (str (get props-with-id :id) "-notification")}
	      props-with-id)))

(defmethod gen-default :type [propkey, properties]
	   (merge
	    {:type 'button}
	    properties))

(defmethod gen-default :text [propkey, properties]
	   (let [props-with-id (gen-default :id properties)]
	     (merge
	      {:text (str (get props-with-id :id))}
	      props-with-id)))


(defmethod gen-default :default [propkey, properties]
	   (println "returning properties" properties)
	   properties)

(defmulti gen-comp
  (fn [props]
    (if-let [type (get props :type)]
      type
      :default)))

(defmethod gen-comp 'panel [properties]
	   (doto (javax.swing.JPanel.)
	     (.setName (get properties :id))))
(defmethod gen-comp 'button [properties]
	   (doto (javax.swing.JButton.)
	     (.setText (get properties :text))
	     (.setName (get properties :id))))
(defmethod gen-comp 'label [properties]
	   (doto (javax.swing.JLabel.)
	     (.setText (get properties :text))
	     (.setName (get properties :id))))
(defmethod gen-comp 'textfield [properties]
	   (doto (javax.swing.JTextField.)
	     (.setText (get properties :text))
	     (.setName (get properties :id))))

(def required-properties [:text :notification :id :type])



(defmulti ensure-props class)
(defmethod ensure-props String [arg] {:type 'label :text arg})
(defmethod ensure-props :default [arg] arg)

(defn swing-generate-with-props
  ([properties]
     (swing-generate-with-props (ensure-props properties) required-properties))
  ([properties checks]
     (if-let [check (first checks)]
       (swing-generate-with-props (gen-default check properties) (rest checks))
       (let [type (get properties :type)
	     id   (get properties :id)
	     text (get properties :text)
	     notification (get properties :notification)]
	 (println "creating swing comp of type" type "id" id "text" text "notification" notification)
	 (gen-comp properties)))))

(defn swing-generate-internal [swing-list]
  (if (empty? swing-list)
    (let [panel (javax.swing.JPanel.)]
      (println "generating panel")
      (.setLayout panel (BoxLayout. panel BoxLayout/X_AXIS))
      panel)
    (let [panel (swing-generate-internal (rest swing-list))
	  this (first swing-list)]
      (println "generating" this)
      (if (instance? String this)
	(doto panel
	  (.add (gen-comp {:type 'label :text this}) ""))
	(.add panel (swing-generate-with-props this) ""))
      panel)))

(defn swing-generate [swing-list]
  (swing-generate-internal (reverse swing-list)))



(defn select-file
  "asks for a file with a swing file chooser"

  ([] (select-file '()))
  ([file_exts] (select-file file_exts (sprop "user.dir")))
  ([file_exts start-dir]
    (println "start dir is" start-dir)
    (let [chooser (JFileChooser.)]
      (doto chooser
        (.setCurrentDirectory (File. start-dir))
        (.setFileFilter (proxy [FileFilter] []
          (accept [file] (any? #(.. file getAbsolutePath (endsWith %1)) file_exts))
          (getDescription [] (str "Files of type " (apply str (interpose ", " file_exts))))
          ))


        )
      (let [result (. chooser showOpenDialog nil)]
          (when (= JFileChooser/APPROVE_OPTION result)
            (. chooser getSelectedFile)))

      )
    )
  )





