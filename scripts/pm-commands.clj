(ns pm-commands)

(load-file "./cljtoolbox.clj")
(refer 'toolboxes.cljtoolbox)

(def pm-types {
  :graph-user-attribute "ua"
  :graph-capabilities "ca"
  :graph-object-attribute "oa"
  :graph-aces "ac"
  :node-user "u"
  :node-user-active-attribute "aa"
  :node-user-attribute "a"
  :node-policy-class "p"
  :node-object-attribute "b" ;Good god, couldn't you have picked some larger identifiers
  :node-association "o"
  :node-opset "s"
  :node-connection "c"
  :node-m-prefix "m"
  :node-intrasession "ai"
  :id "i"
  :arc "r"
  :host "h"
  :object-class "oc"
  :operation "op"
  :object "ob"
  :complex-attribute "cb"
  :attribute-set "as"
  :sac? "sac" ;what is this?
  :session "ses"
  :process "proc"
  :rule "rule"
  :permission "perm"
  :application-path "app"
  :keystore-path "ks"
  :deny "deny"
  :email-account "eml"
  :template "tpl"
  :containers "conts"
  :components "comps"
  :full-name "fn"
  :value "v"
  :record "rec"
  :deny-user-id "user id"
  :deny-user-set "user set"
  :deny-session "session"
  :deny-process "process"
  :deny-inside-session "intra session"
  :deny-across-sessions "across sessions"
  :function "f"
  :unknown "k"
  :script "scr"
  :property "prop"
  :task "tk"
  :scon? "sc"
  :scona? "sca"
  :capability "cap"
  :threshold "th"
  :label "l"
  :object-actions ["create" "delete" "read" "write"]
  :user-actions ["create"]
  :session-actions ["create" "delete"]
  :add "add"
  :assign "asg"
  :object-class-ignored "oc|Ignored"
  })
(def rev-pm-types (apply array-map (interleave (vals pm-types) (keys pm-types))))

(defn encode-to-pmspeak [arg]
  (if-let [code (get pm-types arg)]
    code
    arg))

(defn decode-from-pmspeak [code]
  (if-let [darg (get rev-pm-types code)]
    darg
    code))

(defn command [& args]
  (sjoin "|" (map encode-to-pmspeak args)))
(defn assign [type ptype value parent] (command :assign type value ptype parent))
(def assign-opset-to-objecta (partial assign :node-opset :node-object-attribute))
(def assign-objecta-to-objecta (partial assign :node-object-attribute :node-object-attribute))
(def assign-opset-to-usera (partial assign :node-opset :node-user-attribute))
(def assign-user-to-usera (partial assign :node-user :node-user-attribute))
(defn add-policy-class [name] (command :add :node-policy-class name :node-connection "PM"))
(defn add-objectattr [parenttype objectlabel parentlabel]
  (command :add :node-object-attribute objectlabel parenttype parentlabel))
(defn add-userattr [parenttype userlabel parentlabel]
  (command :add :node-user-attribute userlabel parenttype parentlabel))
(defn add-property [attribute-type key value parent] (command :add :property (str key "=" value) attribute-type parent))
(def add-user-property (partial add-property :node-user-attribute))
(def add-object-property (partial add-property :node-object-attribute))
(defn set-ops [attribute-type attribute-name ops]
  (let [opsetid     (uuid 8)
        createopset (command :add :node-opset opsetid :object-class-ignored attribute-type attribute-name)
        createops   (map (fn mapper [val] (command :add :operation val :node-opset opsetid)) ops)]
      (cons createopset createops)))
(def set-user-ops (partial set-ops :node-user-attribute))
(def set-object-ops (partial set-ops :node-object-attribute))
(defn genops [subject & ops]
  (map #(str subject " " %1) ops))
