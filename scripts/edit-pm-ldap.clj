
(ns gov.nist.csd.pm.scripts
  (:import
    (javax.swing WindowConstants JFrame JButton JTree JLabel SwingUtilities JTextArea JTextField
      Action JScrollPane AbstractAction JMenuBar JMenu JPopupMenu JMenuItem JList ListModel KeyStroke InputMap ActionMap
      JOptionPane JDialog JPanel)
    (javax.swing.event ListSelectionListener)
    (javax.swing.text JTextComponent)
    (javax.swing.tree TreeModel TreeSelectionModel)
    (java.awt Point Dialog Dialog$ModalityType)
    (java.awt.event ActionListener ActionEvent KeyEvent InputEvent MouseAdapter)
    (gov.nist.csd.pm.common.application SysCaller SysCallerImpl SSLSocketClient)
    (gov.nist.csd.pm.common.net ItemType Packet)
    (gov.nist.csd.pm.user CommandUtil)
    (net.miginfocom.swing MigLayout)))

(load-file "./cljtoolbox.clj")
(load-file "./pm-commands.clj")
(refer 'toolboxes.cljtoolbox)
(refer 'pm-commands)

(println "hello world")

(doto (System/getProperties)
  (.setProperty "javax.net.ssl.keyStore" "C:\\PM\\pm1.4\\keystores\\superKeystore")
  (.setProperty "javax.net.ssl.keyStorePassword" "aaaaaa")
  (.setProperty "javax.net.ssl.trustStore" "C:\\PM\\pm1.4\\keystores\\clientTruststore")
  (.setProperty "javax.net.ssl.trustStorePassword", "aaaaaa"))

(def userid "super")
(def vos-view-type (SysCaller/PM_VOS_PRES_ADMIN))
(def client (SSLSocketClient. "localhost" 8081 true "scripter"))
(def sessinfo (CommandUtil/createSession client userid (.toCharArray userid)))
(println "sessinfo")
(def sid (.getSessionId sessinfo))
(def suid (.getUserId sessinfo))
(def process (CommandUtil/createProcess (.getSessionId sessinfo) client))
(println "process")
(def sys-caller (SysCallerImpl. 8081 (.getSessionId sessinfo) process true "clj"))
(println "sys-caller")

(defn send-command-packet [client cmd & args]
  (let [packet (Packet.)]
    (.addItem packet (ItemType/CMD_CODE)cmd)
    (doseq [arg args] (.addItem packet (ItemType/CMD_ARG) arg))
    (println "Issuing command")
    (let [result (.sendReceive client packet (System/out))]
      (println "Command issued")
      (if-let [err (.hasError result)]
      (println "Error in" cmd (.getErrorMessage result))
      '("ok ")))))

(def command-to-ksim (partial send-command-packet client))

(command-to-ksim "computeVos" sid vos-view-type suid sid)

(defn remove-obj [coll pred]
  (remove pred coll))

(defprotocol Contextual
  (is-context? [p o])
  (try-action [p o]))

(def action-key-names
  {:accelerator Action/ACCELERATOR_KEY
   :action_cmd Action/ACTION_COMMAND_KEY
   :default Action/DEFAULT
   :long Action/LONG_DESCRIPTION
   :mnemonic Action/MNEMONIC_KEY
   :short Action/SHORT_DESCRIPTION
   :sm_icon Action/SMALL_ICON
   :name Action/NAME})

(defn action
  ([name map f]
    (let [act (proxy [AbstractAction] []
                    (actionPerformed [ae] (f ae)))]
      (doseq [k (keys map)]
        (println "Putting value" (get action-key-names k) (get map k))
        (.putValue act (str (get action-key-names k)) (str (get map k))))
      (println "returning action" act)
      act))
  ([name f]
    (action name {:name name} f)))

(defn action-event [o action]
  (ActionEvent. o 0 (.getValue action Action/ACTION_COMMAND_KEY)))

(deftype ContextAction [action test]
  Contextual
  (is-context? [p o] (test o))
  (try-action [p o] (when-let [is-ctx (is-context? p o)] (.actionPerformed action (action-event o )))))

(defn proxy-action
  "Creates an Action wrapper that calls postfn after calling the proxied Action's
   actionPerformed method"
  [action postfn]
  (proxy [Action][]
    (addPropertyChangeListener [pcl]    (.addPropertyChangeListener action pcl))
    (removePropertyChangeListener [pcl] (.removePropertyChangeListener action pcl))
    (getValue [key]       (.getValue action key))
    (putValue [key value] (.putValue action key value))
    (isEnabled []         (.isEnabled action))
    (setEnabled [e]       (.setEnabled action e))
    (actionPerformed [ae]
      (.actionPerformed action ae)
      (postfn ae))))

(defn modal-dialog
  "Constructs a modal dialog with a content panel and a set of actions
   Each action will be represented by a button on the bottom of the dialog.
   Pressing any of the buttons will close the dialog"
  [title content & rawactions]
    (let [awindow   (get-active-window)
          dialog    (JDialog. awindow title Dialog$ModalityType/APPLICATION_MODAL)
          closefn   (fn [ae] (.setVisible dialog false))
          actions   (map #(proxy-action % closefn) rawactions)
          but-first (rest actions)
          split-str (str "split " (count actions))]
      (doto dialog
        (.setLayout (MigLayout. "" "[fill, grow][]" ""))
        (.add content "wrap")
        (.add (JButton. (first actions)) split-str)
        (.setLocationRelativeTo awindow))
      (doseq [action but-first]
        (.add dialog (JButton. action)))
      dialog))

(defn present-request-dialog
  "Method for getting information from the user
  Input a title and some property names and this
  method will present a modal dialog to gather input
  and return that input as a list."
  [title & propnames]
  (let [panel   (doto (JPanel.) (.setLayout (MigLayout. "" "[][fill, grow]" "")))
        comps   (map (fn [name] [(JLabel. name)(JTextField. 30)]) propnames)]
    (doseq [pair comps]
        (doto panel
          (.add (first pair) "label")
          (.add (second pair) "wrap")))
    (let [result  (atom false)
          dialog  (modal-dialog title panel
                    (action "OK" (fn [ae] (reset! result true)))
                    (action "Cancel" (fn [ae] (reset! result false))))]
      (doto dialog
        (.pack)
        (.setVisible true))
      (if @result
        (map #(.getText (second %)) comps)
        []))))

(defprotocol Node
  (get-output [p])
  (get-label [p])
  (get-id [p])
  (get-type [p]))

(deftype PMNode [type id label]
  Node
  (get-output [p] (str "Node of class " (class p) "Type: " type "ID: " id "Label: " label))
  (get-label [p] label)
  (get-id [p] id)
  (get-type [p] type)
  Object
  (toString [self] label))

(defn third
  "Returns the third element of a list or the 2nth :-)"
  [coll] (nth coll 2))

(defn get-pos-members-of
  "returns the pos members of a VOS constructed for this session.
  Note: a session must be created and a vos constructed before calling this method.
  Otherwise nothing will be returned"
  [sys-caller node]
  (println (get-output node))
  (println (str "SysCaller " sys-caller))
  (map
    #(PMNode. (first %1) (second %) (third %))
    (.getPosMembersOf sys-caller (get-label node) (get-id node) (get-type node) vos-view-type)))

(defn graph-model [sys-caller]
  "Returns a TreeModel of the current sessions VOS
  Only loads the currently expanded levels of the VOS"
  (let [listeners (atom '())
        get-pos-members (memoize (partial get-pos-members-of sys-caller))]
    (proxy [TreeModel] []
      (addTreeModelListener [l] (swap! listeners conj l))
      (getChild [parent index] (nth (get-pos-members parent) index))
      (getChildCount [parent] (count (get-pos-members  parent)))
      (getIndexOfChild [parent child] (.indexOf child (get-pos-members parent)) )
      (getRoot [] (PMNode. (SysCaller/PM_NODE_CONN) (SysCaller/PM_CONNECTOR_ID) (SysCaller/PM_CONNECTOR_NAME)))
      (isLeaf [node] (empty? (get-pos-members node)))
      (removeTreeModelListener [l] (swap! listeners remove-obj l))
      (valueForPathChanged[path newValue]))))


(defn default-scroll-pane [view]
  "Just a scrollpane.  These defaults are more useful
  than the ones set in the constructor."
  (doto (JScrollPane. view)
    (.setHorizontalScrollBarPolicy JScrollPane/HORIZONTAL_SCROLLBAR_AS_NEEDED)
    (.setVerticalScrollBarPolicy JScrollPane/VERTICAL_SCROLLBAR_ALWAYS)
    (.. getVerticalScrollBar (setUnitIncrement 10))
    (.. getVerticalScrollBar (setBlockIncrement 10))
    (.. getHorizontalScrollBar (setUnitIncrement 10))
    (.. getHorizontalScrollBar (setBlockIncrement 10))))


(defn list-model [listfn]
  "Takes listfn, a function with no arguments, and expects that function
  to return a sequence or list.  That returned list is used to populate the
  ListModel"
  (let [listeners (atom [])]
    (proxy [ListModel] []
      (addListDataListener [l] (swap! listeners conj l))
      (removeListDataListener [l] (swap! listeners remove-obj l))
      (getElementAt [i] (nth (listfn) i))
      (getSize [] (count (listfn))))))

(defn list-selection-listener [selectionfn]
  (proxy [ListSelectionListener] []
    (valueChanged [e]
      (if (instance? JList (.getSource e))
        (println "is jlist")
        (println "is not jlist"))
      (println "SelectionChanged" e)
      (selectionfn e))))

(defn listing-window [listfn selectionfn actionEvent]
  (doto (JFrame. "listing window")
    (.setLayout (MigLayout. "" "[fill, grow]" "[fill, grow]"))
    (.add (default-scroll-pane
      (doto (JList.)
        (.setModel
          (list-model listfn))
        (.addListSelectionListener (list-selection-listener selectionfn)))))
    (.setSize 400 400)
    (.setVisible true)))

(defn list-detail-window [listfn detailfn actionEvent]
  (let [window   (get-active-window)
        textview (doto (JTextArea. ) (.setEnabled false))
        selfn    (fn selected [e]
                   (println "list detail selected")
                   (let [details (detailfn (-> e .getSource .getSelectedValue))]
                     (println (class details) details)
                     (.setText textview details)))]
  (doto (listing-window listfn selfn actionEvent)
    (.setLocationRelativeTo window)
    (.add textview "newline"))))


(defn template-list [sys-caller]
  (-> sys-caller .getTemplates .getStringValues))

(defn get-entity-name [sys-caller eid]
  (println "getting named entity" eid)
  (.getEntityName sys-caller eid SysCaller/PM_NODE_OATTR))

(defn template-detail [sys-caller template-info]
  (let [tid (second (split ":" template-info))
        tinfo (-> sys-caller (.getTemplateInfo tid) (.getStringValues))
        splits (->> tinfo second (split ":"))
        ]
    (println "getting template detail" tid "from template list" template-info " splits " (apply str splits) )
    (str (sjoin "\n" tinfo) "\n" (sjoin ", " (map (partial get-entity-name sys-caller) splits)))))


(defn execute-command [sys-caller textsource actionEvent]
  (let [res (->> textsource (.getText) (eval))]
    (println res)
    (load-string res)))

(defn executor-window [sys-caller actionEvent]
  (let [textarea (JTextArea.)
        execute-action (action "Execute" (partial execute-command sys-caller textarea))
        inputmap (.getInputMap textarea)
        actionmap (.getActionMap textarea)
        exactionname "exec-action"
        keystroke (KeyStroke/getKeyStroke KeyEvent/VK_ENTER InputEvent/SHIFT_DOWN_MASK)]
    (println "putting inputmap in place of" (.get inputmap keystroke))
    (.put inputmap keystroke exactionname)
    (println "replaces input action with" (.get inputmap keystroke))
    (println "putting actionmap")
    (.put actionmap exactionname execute-action)
    (doto (JFrame.)
      (.setSize 800 400)
      (.setLayout (MigLayout. "" "[grow, fill][align left]" "[grow, fill][]"))
      (.add (default-scroll-pane textarea))
      (.add (JButton. execute-action) "newline")
      (.setVisible true))))

(defn make-menu [sys-caller]
  (let [listfn          (partial template-list sys-caller)
        detailfn        (partial template-detail sys-caller)
        template-window (partial list-detail-window listfn detailfn)]
  (doto (JMenuBar.)
    (.add (doto (JMenu. "File")
      (.add (doto (JMenuItem. "Open...")))
      (.add (doto (JMenuItem. (action "Exit" (fn exit [ae] (System/exit 0))))))
      (.add (JMenuItem. (action "Show Templates..." template-window)))
      (.add (JMenuItem. (action "Executor..." (partial executor-window sys-caller)))))))))

(defmulti get-context-for (fn [obj x y]
  (println "classifying" obj x y)
  (class obj)))
(defmethod get-context-for JList [obj x y]
  (println "is jlist")
  (.locationToIndex obj (Point. x y)))
(defmethod get-context-for JTree [obj x y]
  (println "is jtree")
  (if-let [path (.getPathForLocation obj x y)]
    (.getLastPathComponent path)
    nil))
(defmethod get-context-for :default [obj x y]
  (println "oops")
  nil)

(defn add-property-action [sys-caller pmnode actionEvent]
  (let [props (present-request-dialog "Input Property" "Name" "Value")
        cmd   (add-property (decode-from-pmspeak (get-type pmnode))
                (first props)
                (second props)
                (get-label pmnode))]
    (println "Prop Values Retrieved" props)
    (println "Command Constructed" cmd)))

(defn add-object-action [sys-caller pmnode actionEvent]
  (let [props (present-request-dialog (str "Add object under" (get-label pmnode)) "Object Name")
        sclass "Object attribute"
        includesAscendants  "yes"
        host (get-label pmnode)
        totype "p"
        toname (get-policy-class pmnode)
        cmd (add-object (decode-from-pmspeak (get-type pmnode))
              (first props)
              (second sprops)
              (get-label pmnode))]
    ))

(defmulti context-menu-maker (fn [obj sc] (class obj)))
(defmethod context-menu-maker PMNode [pmnode sys-caller]
  (println "need to supply actions for pm node")
  (doto (JPopupMenu.)
    (.add (JMenuItem. (action "Add Property" (partial add-property-action sys-caller pmnode))))
    (.add (JMenuItem. (action "Add Object" (partial add-object-action sys-caller pmnode))))
    (.add (JMenuItem. "Add File Object"))
    (.add (JMenuItem. "Copy Object"))
    (.add (JMenuItem. "Paste Object"))))
(defmethod context-menu-maker :default [obj sys-caller]
  (println "no menu defined for object of type" (class obj) obj)
  (JPopupMenu.))



(defn window [sys-caller]
  (println "showing window")
  (let [frame         (JFrame. "Viewer")
        menu          (make-menu sys-caller)
        tree          (JTree. (graph-model sys-caller))
        check-for-pop (fn [me] (
                        let  [source  (.getSource me)
                              point   (.getPoint me)
                              x       (.getX point)
                              y       (.getY point)]
                          (println "is is being triggered?")
                          (if (.isPopupTrigger me)
                            (when-let [context (get-context-for source x y)]
                              (doto (context-menu-maker context sys-caller)
                                (.show (.getComponent me) x y)))
                            (println "not a poppler"))))]
    (.addMouseListener tree (proxy [MouseAdapter] []
      (mousePressed [me]
        (println "Pressed check"))
      (mouseReleased [me]
        (println "Released check")
        (check-for-pop me))))
    (doto frame
      (.setSize 400 400)
      (.setLayout (MigLayout. "" "[fill, grow]" "[fill, grow]"))
      (.add (default-scroll-pane tree))
      (.setJMenuBar menu)
      (.setDefaultCloseOperation (WindowConstants/EXIT_ON_CLOSE))
      (.setVisible true))))


(def rt (Runtime/getRuntime))
(.addShutdownHook rt
  (proxy [Thread] []
    (run [] (println "goodbye")
      (CommandUtil/exitProcess sid process client)
      (CommandUtil/exitSession sid client))))
(SwingUtilities/invokeLater (partial window sys-caller))

(println "done")


