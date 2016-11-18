add|p|MLS|c|PM
add|prop|type=mls|p|MLS
add|prop|levels=Secret,Top secret|p|MLS
add|p|DAC|c|PM
add|prop|type=discretionary|p|DAC
add|p|RBAC|c|PM
add|prop|type=rbac|p|RBAC
add|p|Confine|c|PM
add|prop|type=confinement|p|Confine
add|a|Secret|p|MLS
add|prop|correspondsto=S|a|Secret
add|ob|TS rep|Object attribute|no|TS|FADB82D07EA31058768089FE80555193|p|MLS
add|ob|S rep|Object attribute|no|S|D3C1128FAFEBC06DF38556B3F8EF918F|p|MLS
add|b|S_TS|p|MLS
add|a|Acct Mgr|p|DAC
add|a|Acct Repr|p|DAC
add|a|Nurse|p|DAC
add|a|Adm Clerk|p|DAC
add|a|DAC uattrs|p|DAC
add|prop|usersof=discretionary|a|DAC uattrs
add|ob|Acct Recs rep|Object attribute|yes|Acct Recs|188A0BB079AF3DBF85B0EE2A164ECB67|p|DAC
add|ob|Alice Med Records rep|Object attribute|yes|Alice Med Records|EEF24065161E94E7E05703BC7FE241F0|p|DAC
add|ob|Bob Med Records rep|Object attribute|yes|Bob Med Records|0C99DCA0335B14CD5077FA4BE62DF08C|p|DAC
add|ob|Populated Forms rep|Object attribute|no|Populated Forms|C6FAE128F13E4C90ACC6E66036A4E746|p|DAC
add|ob|katie home rep|Object attribute|yes|katie home|17F18D645269BD3BEDAE0722B3FCB0DD|p|DAC
add|ob|inboxes rep|Object attribute|yes|inboxes|20DA0F7247F81B6F7A6E6862C54AEF6B|p|DAC
add|ob|charlie home rep|Object attribute|yes|charlie home|ED83F81124A17394318ED9E2C735402C|p|DAC
add|ob|bob home rep|Object attribute|yes|bob home|2D6254EEBF35D184B426230EE6820D16|p|DAC
add|ob|alice home rep|Object attribute|yes|alice home|265806DD716BE58F262345FB97A5E5D0|p|DAC
add|ob|CMRecs rep|Object attribute|yes|CMRecs|AF9D5F253807CB3E3F53D74B22B16893|p|DAC
add|b|Acct Recs|p|DAC
add|b|Today|p|DAC
add|b|Populated Forms|p|DAC
add|b|witems|p|DAC
add|prop|containerof=witems|b|witems
add|b|katie home|p|DAC
add|prop|homeof=katie|b|katie home
add|b|inboxes|p|DAC
add|prop|containerof=inboxes|b|inboxes
add|b|charlie home|p|DAC
add|prop|homeof=charlie|b|charlie home
add|b|bob home|p|DAC
add|prop|homeof=bob|b|bob home
add|b|alice home|p|DAC
add|prop|homeof=alice|b|alice home
add|ob|DAC uattrs rep|User attribute|yes|DAC uattrs|A7E1D543C7EDE55B428047BFAA6F096F|p|DAC
add|b|outboxes|p|DAC
add|prop|containerof=outboxes|b|outboxes
add|b|CMRecs|p|DAC
asg|a|Acct Repr|p|RBAC
asg|a|Acct Mgr|p|RBAC
asg|a|Nurse|p|RBAC
asg|a|Adm Clerk|p|RBAC
add|a|Accts Pbl|p|RBAC
add|a|Contracting|p|RBAC
add|a|Accts Rcv|p|RBAC
add|a|Acquisition|p|RBAC
add|a|Secretary|p|RBAC
add|a|Intern|p|RBAC
add|ob|Acct Columns rep|Object attribute|yes|Acct Columns|73F0ADFD2AA8BE10A48496084F96EE0B|p|RBAC
add|ob|CMR Columns rep|Object attribute|yes|CMR Columns|0D11990CD2E061510491DA7611A1042A|p|RBAC
add|b|Acct Columns|p|RBAC
add|b|CMR Columns|p|RBAC
add|b|Accts Pbl witems|p|RBAC
add|prop|witemsof=Accts Pbl|b|Accts Pbl witems
add|b|Contracting witems|p|RBAC
add|prop|witemsof=Contracting|b|Contracting witems
add|b|Accts Rcv witems|p|RBAC
add|prop|witemsof=Accts Rcv|b|Accts Rcv witems
add|b|Approved Orders|p|RBAC
add|b|Forms|p|RBAC
add|b|Med Records|p|RBAC
add|a|OU users|p|Confine
add|ob|OU messages rep|Object attribute|yes|OU messages|1BAE7D8BC29117AC89D714C0DC3BCFB5|p|Confine
add|b|OU messages|p|Confine
add|u|bob|fn|Robert|a|Secret
add|eml|Robert|bob@nist.gov|email.nist.gov|email.nist.gov|nist|bob|u|bob
add|a|Top secret|a|Secret
add|prop|correspondsto=TS|a|Top secret
add|s|033A0D1C|oc|Ignored|a|Secret
add|op|Object attribute assign to|s|033A0D1C
add|op|Object attribute create object|s|033A0D1C
add|s|C2CF01CD|oc|Ignored|a|Secret
add|op|File read|s|C2CF01CD
add|s|20DE6FEB|oc|Ignored|a|Secret
add|op|File write|s|20DE6FEB
add|s|650F246B|oc|Ignored|b|TS rep
add|op|Object attribute assign to|s|650F246B
add|op|Object attribute create object|s|650F246B
asg|s|033A0D1C|b|S rep
add|b|S|b|S_TS
add|b|TS|b|S_TS
asg|s|20DE6FEB|b|S_TS
add|s|78E5ABCE|oc|Ignored|b|S_TS
add|op|File read|s|78E5ABCE
add|u|katie|fn|Katherine|a|Acct Mgr
add|eml|Katherine|katherine.macfarland@nist.gov|email.nist.gov|email.nist.gov|nist|katie|u|katie
add|s|5791E4F9|oc|Ignored|a|Acct Mgr
add|op|Object attribute assign to|s|5791E4F9
add|op|Object attribute create object|s|5791E4F9
add|op|Object attribute create object attribute|s|5791E4F9
add|s|0F404D67|oc|Ignored|a|Acct Mgr
add|op|File read|s|0F404D67
add|op|File write|s|0F404D67
add|s|47CE9638|oc|Ignored|a|Acct Mgr
add|op|Object attribute assign|s|47CE9638
add|op|Object attribute create object|s|47CE9638
add|s|8B31D32C|oc|Ignored|a|Acct Mgr
add|op|File read|s|8B31D32C
add|op|File write|s|8B31D32C
add|s|3E5AF6F1|oc|Ignored|a|Acct Mgr
add|op|File read|s|3E5AF6F1
add|op|File write|s|3E5AF6F1
add|s|F6CCB92B|oc|Ignored|a|Acct Mgr
add|op|File read|s|F6CCB92B
add|op|File write|s|F6CCB92B
add|s|581A03EB|oc|Ignored|a|Acct Mgr
add|op|File read|s|581A03EB
add|op|File write|s|581A03EB
add|u|alice|fn|Alicia|a|Acct Repr
add|eml|Alicia|alice@nist.gov|email.nist.gov|email.nist.gov|nist|alice|u|alice
add|s|20B9C61C|oc|Ignored|a|Acct Repr
add|op|File read|s|20B9C61C
add|op|File write|s|20B9C61C
add|s|CAF1CC9C|oc|Ignored|a|Acct Repr
add|op|File read|s|CAF1CC9C
add|op|File write|s|CAF1CC9C
add|s|0AA431B8|oc|Ignored|a|Acct Repr
add|op|File read|s|0AA431B8
add|op|File write|s|0AA431B8
add|s|EA3DBB84|oc|Ignored|a|Acct Repr
add|op|File read|s|EA3DBB84
add|op|File write|s|EA3DBB84
add|s|AD179DE1|oc|Ignored|a|Acct Repr
add|op|File read|s|AD179DE1
add|op|File write|s|AD179DE1
add|s|4D72BCE2|oc|Ignored|a|Nurse
add|op|File read|s|4D72BCE2
add|op|File write|s|4D72BCE2
add|s|0B29B5B1|oc|Ignored|a|Nurse
add|op|File read|s|0B29B5B1
add|s|6523425D|oc|Ignored|a|Nurse
add|op|File read|s|6523425D
add|s|D36773F1|oc|Ignored|a|Nurse
add|op|File read|s|D36773F1
add|s|C29A03C7|oc|Ignored|a|Nurse
add|op|File read|s|C29A03C7
add|op|File write|s|C29A03C7
add|s|7F91A035|oc|Ignored|a|Nurse
add|op|File read|s|7F91A035
add|s|4E95A4D5|oc|Ignored|a|Nurse
add|op|File read|s|4E95A4D5
add|op|File write|s|4E95A4D5
asg|u|katie|a|Adm Clerk
add|s|1DDA9933|oc|Ignored|a|Adm Clerk
add|op|File read|s|1DDA9933
add|op|File write|s|1DDA9933
add|s|C5F57D1C|oc|Ignored|a|Adm Clerk
add|op|Object attribute assign|s|C5F57D1C
add|op|Object attribute create object|s|C5F57D1C
add|s|9CD521EB|oc|Ignored|a|Adm Clerk
add|op|File write|s|9CD521EB
add|s|0BC643D6|oc|Ignored|a|Adm Clerk
add|op|File read|s|0BC643D6
add|op|File write|s|0BC643D6
add|s|233650FB|oc|Ignored|a|Adm Clerk
add|op|File read|s|233650FB
add|op|File write|s|233650FB
add|s|AD27716D|oc|Ignored|a|Adm Clerk
add|op|Object attribute assign to|s|AD27716D
add|op|Object attribute create object attribute|s|AD27716D
add|op|Object attribute create object|s|AD27716D
add|a|David|a|DAC uattrs
add|prop|nameof=dave|a|David
add|a|Katherine|a|DAC uattrs
add|prop|nameof=katie|a|Katherine
add|a|Charles|a|DAC uattrs
add|prop|nameof=charlie|a|Charles
add|a|Robert|a|DAC uattrs
add|prop|nameof=bob|a|Robert
add|a|Alicia|a|DAC uattrs
add|prop|nameof=alice|a|Alicia
add|a|Exporter|a|DAC uattrs
add|prop|nameof=exporter|a|Exporter
add|s|D31DD406|oc|Ignored|a|DAC uattrs
add|op|Object attribute create object|s|D31DD406
add|s|A1F3F938|oc|Ignored|a|DAC uattrs
add|op|File write|s|A1F3F938
add|s|E963428D|oc|Ignored|a|DAC uattrs
add|op|Object attribute assign to|s|E963428D
asg|s|5791E4F9|b|Acct Recs rep
asg|s|D31DD406|b|Populated Forms rep
add|s|0B4407A1|oc|Ignored|b|katie home rep
add|op|Operation set assign to|s|0B4407A1
add|op|Operation set assign|s|0B4407A1
add|op|Entity represent|s|0B4407A1
add|op|Object attribute assign|s|0B4407A1
add|op|Object attribute create operation set|s|0B4407A1
add|op|Object attribute assign to|s|0B4407A1
add|op|Object attribute create object|s|0B4407A1
add|op|Object attribute create object attribute|s|0B4407A1
asg|s|E963428D|b|inboxes rep
add|s|ACD130E8|oc|Ignored|b|charlie home rep
add|op|Entity represent|s|ACD130E8
add|op|Operation set assign to|s|ACD130E8
add|op|Object attribute create object attribute|s|ACD130E8
add|op|Object attribute assign|s|ACD130E8
add|op|Object attribute create object|s|ACD130E8
add|op|Operation set assign|s|ACD130E8
add|op|Object attribute assign to|s|ACD130E8
add|op|Object attribute create operation set|s|ACD130E8
add|s|A0BD0694|oc|Ignored|b|bob home rep
add|op|Object attribute create operation set|s|A0BD0694
add|op|Object attribute assign to|s|A0BD0694
add|op|Object attribute create object attribute|s|A0BD0694
add|op|Operation set assign to|s|A0BD0694
add|op|Object attribute assign|s|A0BD0694
add|op|Object attribute create object|s|A0BD0694
add|op|Entity represent|s|A0BD0694
add|op|Operation set assign|s|A0BD0694
add|s|94C9DF0B|oc|Ignored|b|alice home rep
add|op|Entity represent|s|94C9DF0B
add|op|Object attribute create operation set|s|94C9DF0B
add|op|Object attribute create object|s|94C9DF0B
add|op|Object attribute create object attribute|s|94C9DF0B
add|op|Object attribute assign|s|94C9DF0B
add|op|Operation set assign to|s|94C9DF0B
add|op|Object attribute assign to|s|94C9DF0B
add|op|Operation set assign|s|94C9DF0B
asg|s|AD27716D|b|CMRecs rep
add|b|12345678|b|Acct Recs
asg|s|20B9C61C|b|Acct Recs
asg|s|0F404D67|b|Acct Recs
asg|s|A1F3F938|b|Populated Forms
add|b|katie witems|b|witems
add|prop|witemsof=katie|b|katie witems
add|b|charlie witems|b|witems
add|prop|witemsof=charlie|b|charlie witems
add|b|bob witems|b|witems
add|prop|witemsof=bob|b|bob witems
add|b|alice witems|b|witems
add|prop|witemsof=alice|b|alice witems
add|s|73F33EBB|oc|Ignored|b|katie home
add|op|File read|s|73F33EBB
add|op|File write|s|73F33EBB
add|b|katie INBOX|b|inboxes
add|prop|inboxof=katie|b|katie INBOX
add|b|OU inboxes|b|inboxes
add|b|SharedContainer|b|charlie home
add|s|663A07F7|oc|Ignored|b|charlie home
add|op|File write|s|663A07F7
add|op|File read|s|663A07F7
add|b|Bob Med Records|b|bob home
add|prop|patientsof=bob|b|Bob Med Records
asg|b|SharedContainer|b|bob home
add|s|9F00F0A0|oc|Ignored|b|bob home
add|op|File read|s|9F00F0A0
add|op|File write|s|9F00F0A0
asg|b|SharedContainer|b|alice home
add|b|Proposals|b|alice home
add|b|Alice Med Records|b|alice home
add|prop|patientsof=alice|b|Alice Med Records
add|s|B254FA19|oc|Ignored|b|alice home
add|op|File read|s|B254FA19
add|op|File write|s|B254FA19
add|s|12345678|oc|Ignored|b|DAC uattrs rep
add|op|User assign|s|12345678
add|op|User attribute assign to operation set|s|12345678
add|s|15E69BE1|oc|Ignored|b|DAC uattrs rep
add|op|User assign|s|15E69BE1
add|op|User attribute assign to operation set|s|15E69BE1
add|s|EE568F88|oc|Ignored|b|DAC uattrs rep
add|op|User attribute assign to operation set|s|EE568F88
add|op|User assign|s|EE568F88
add|s|60507C01|oc|Ignored|b|DAC uattrs rep
add|op|User attribute assign to operation set|s|60507C01
add|op|User assign|s|60507C01
add|ob|katie OUTBOX rep|Object attribute|yes|katie OUTBOX|1C62EB60708A54FB2FB32CFB9A66B123|b|outboxes
add|b|OU outboxes|b|outboxes
add|b|katie OUTBOX|b|outboxes
add|prop|outboxof=katie|b|katie OUTBOX
asg|b|Alice Med Records|b|CMRecs
asg|b|Bob Med Records|b|CMRecs
asg|s|4D72BCE2|b|CMRecs
asg|s|1DDA9933|b|CMRecs
add|u|charlie|fn|Charles|a|Accts Pbl
add|eml|Charles|charlie@nist.gov|email.nist.gov|email.nist.gov|nist|charlie|u|charlie
asg|u|alice|a|Accts Pbl
add|s|16A11DA2|oc|Ignored|a|Accts Pbl
add|op|File read|s|16A11DA2
add|op|File write|s|16A11DA2
asg|u|bob|a|Contracting
add|s|18E9B035|oc|Ignored|a|Contracting
add|op|File read|s|18E9B035
add|op|File write|s|18E9B035
asg|u|alice|a|Accts Rcv
add|s|B3FD2394|oc|Ignored|a|Accts Rcv
add|op|File read|s|B3FD2394
add|op|File write|s|B3FD2394
add|u|dave|fn|David|a|Acquisition
add|s|D6F23181|oc|Ignored|a|Acquisition
add|op|File read|s|D6F23181
asg|u|katie|a|Secretary
add|s|D7048A1C|oc|Ignored|a|Secretary
add|op|File read|s|D7048A1C
add|op|File write|s|D7048A1C
add|a|Doctor|a|Intern
add|s|945358F8|oc|Ignored|a|Intern
add|op|File read|s|945358F8
asg|s|47CE9638|b|Acct Columns rep
asg|s|C5F57D1C|b|CMR Columns rep
add|b|AcctAddr|b|Acct Columns
add|b|AcctSsn|b|Acct Columns
add|b|AcctName|b|Acct Columns
add|b|AcctNum|b|Acct Columns
add|b|PatTreatment|b|CMR Columns
add|b|PatDiag|b|CMR Columns
add|b|PatSymptoms|b|CMR Columns
add|b|PatAllergies|b|CMR Columns
add|b|PatHistory|b|CMR Columns
add|b|PatBio|b|CMR Columns
add|b|PatId|b|CMR Columns
asg|s|16A11DA2|b|Accts Pbl witems
asg|s|18E9B035|b|Contracting witems
asg|s|B3FD2394|b|Accts Rcv witems
asg|s|D6F23181|b|Approved Orders
add|ob|poForm|File|no|ws-sys|C:\pmworkArea\poForm.wkf|b|Forms
asg|s|D7048A1C|b|Forms
add|ob|mrec33|File|no|ws-sys|C:\pmworkArea\mrec33.rtf|b|Med Records
add|ob|mrec22|File|no|ws-sys|C:\pmworkArea\mrec22.rtf|b|Med Records
add|ob|mrec11|File|no|ws-sys|C:\pmworkArea\mrec11.rtf|b|Med Records
add|ob|mrec3|File|no|ws-sys|C:\pmworkArea\mrec3.doc|b|Med Records
add|ob|mrec2|File|no|ws-sys|C:\pmworkArea\mrec2.doc|b|Med Records
add|ob|mrec1|File|no|ws-sys|C:\pmworkArea\mrec1.doc|b|Med Records
add|ob|mrec4|File|no|ws-sys|C:\pmworkArea\mrec4.doc|b|Med Records
add|ob|mrec5|File|no|ws-sys|C:\pmworkArea\mrec5.rtf|b|Med Records
asg|s|945358F8|b|Med Records
add|s|5C59BE03|oc|Ignored|b|Med Records
add|op|File write|s|5C59BE03
asg|u|charlie|a|OU users
asg|u|bob|a|OU users
asg|u|alice|a|OU users
add|s|359268CA|oc|Ignored|a|OU users
add|op|Object attribute assign|s|359268CA
add|op|Object attribute assign to|s|359268CA
add|op|Object attribute create object|s|359268CA
add|s|EBBE3FE1|oc|Ignored|a|OU users
add|op|File read|s|EBBE3FE1
add|op|File write|s|EBBE3FE1
asg|s|359268CA|b|OU messages rep
asg|s|EBBE3FE1|b|OU messages
asg|u|alice|a|Top secret
asg|a|Top secret|s|650F246B
add|s|61FAF91D|oc|Ignored|a|Top secret
add|op|File write|s|61FAF91D
asg|a|Top secret|s|78E5ABCE
asg|b|mrec22|b|S
asg|b|mrec2|b|S
asg|b|mrec4|b|S
asg|s|C2CF01CD|b|S
asg|b|mrec11|b|TS
asg|b|mrec1|b|TS
asg|s|61FAF91D|b|TS
asg|u|dave|a|David
asg|u|dave|a|Nurse
asg|u|katie|a|Katherine
asg|a|Katherine|s|0B4407A1
add|s|61538005|oc|Ignored|a|Katherine
add|op|File write|s|61538005
add|s|7F85FF96|oc|Ignored|a|Katherine
add|op|File read|s|7F85FF96
add|s|11AE564E|oc|Ignored|a|Katherine
add|op|File read|s|11AE564E
add|s|39D1BFC8|oc|Ignored|a|Katherine
add|op|File read|s|39D1BFC8
add|op|File write|s|39D1BFC8
asg|a|Katherine|s|12345678
add|s|708FA799|oc|Ignored|a|Katherine
add|op|Object attribute assign|s|708FA799
add|op|Object attribute assign to|s|708FA799
asg|a|Katherine|s|73F33EBB
asg|u|charlie|a|Charles
asg|a|Charles|s|ACD130E8
add|s|D041A88C|oc|Ignored|a|Charles
add|op|File write|s|D041A88C
add|s|E2327C18|oc|Ignored|a|Charles
add|op|File read|s|E2327C18
add|op|File write|s|E2327C18
add|s|B270223E|oc|Ignored|a|Charles
add|op|Object attribute assign|s|B270223E
add|op|Object attribute assign to|s|B270223E
add|s|BCDE77D3|oc|Ignored|a|Charles
add|op|File read|s|BCDE77D3
asg|a|Charles|s|15E69BE1
asg|a|Charles|s|663A07F7
add|s|94E4DFD4|oc|Ignored|a|Charles
add|op|File read|s|94E4DFD4
asg|u|bob|a|Robert
asg|a|Robert|s|A0BD0694
add|s|B12DFFDD|oc|Ignored|a|Robert
add|op|File write|s|B12DFFDD
add|s|FE4A7080|oc|Ignored|a|Robert
add|op|File read|s|FE4A7080
add|op|File write|s|FE4A7080
add|s|504C2DFC|oc|Ignored|a|Robert
add|op|Object attribute assign|s|504C2DFC
add|op|Object attribute assign to|s|504C2DFC
add|s|19C91A87|oc|Ignored|a|Robert
add|op|File read|s|19C91A87
asg|a|Robert|s|EE568F88
asg|a|Robert|s|9F00F0A0
add|s|F7D14F3F|oc|Ignored|a|Robert
add|op|File read|s|F7D14F3F
asg|u|alice|a|Alicia
asg|a|Alicia|s|94C9DF0B
add|s|15068202|oc|Ignored|a|Alicia
add|op|File write|s|15068202
add|s|1FAC2FCC|oc|Ignored|a|Alicia
add|op|File read|s|1FAC2FCC
add|op|File write|s|1FAC2FCC
add|s|87499089|oc|Ignored|a|Alicia
add|op|Object attribute assign|s|87499089
add|op|Object attribute assign to|s|87499089
asg|a|Alicia|s|60507C01
asg|a|Alicia|s|B254FA19
add|s|1698910F|oc|Ignored|a|Alicia
add|op|File read|s|1698910F
add|s|BDB987A8|oc|Ignored|a|Alicia
add|op|File read|s|BDB987A8
add|u|exporter|fn|Exporter|a|Exporter
add|ob|FC15B612|File|no|ws-sys|C:\pmworkArea\FC15B612.txt|b|12345678
add|ob|C9CFE6DE|File|no|ws-sys|C:\pmworkArea\C9CFE6DE.txt|b|12345678
add|ob|30A44CB5|File|no|ws-sys|C:\pmworkArea\30A44CB5.txt|b|12345678
add|ob|237D8FA7|File|no|ws-sys|C:\pmworkArea\237D8FA7.txt|b|12345678
asg|s|39D1BFC8|b|katie witems
asg|s|E2327C18|b|charlie witems
asg|s|FE4A7080|b|bob witems
asg|s|1FAC2FCC|b|alice witems
add|b|katie wINBOX|b|katie INBOX
add|prop|winboxof=katie|b|katie wINBOX
asg|s|7F85FF96|b|katie INBOX
add|b|alice INBOX|b|OU inboxes
add|prop|inboxof=alice|b|alice INBOX
add|b|charlie INBOX|b|OU inboxes
add|prop|inboxof=charlie|b|charlie INBOX
add|b|bob INBOX|b|OU inboxes
add|prop|inboxof=bob|b|bob INBOX
add|b|Charlie recipes|b|SharedContainer
asg|b|mrec4|b|Bob Med Records
asg|b|mrec5|b|Bob Med Records
add|b|8E094FF2|b|Bob Med Records
add|ob|prop1|File|no|ws-sys|C:\pmworkArea\prop1.rtf|b|Proposals
asg|b|mrec11|b|Alice Med Records
asg|b|mrec22|b|Alice Med Records
asg|b|mrec33|b|Alice Med Records
asg|b|mrec1|b|Alice Med Records
asg|b|mrec2|b|Alice Med Records
asg|b|mrec3|b|Alice Med Records
add|b|8B54E24B|b|Alice Med Records
asg|s|708FA799|b|katie OUTBOX rep
add|ob|charlie OUTBOX rep|Object attribute|yes|charlie OUTBOX|608654BCBDD02256D1C034690A6613DC|b|OU outboxes
add|ob|bob OUTBOX rep|Object attribute|yes|bob OUTBOX|3A1CF076CCA56F130B1E2136BC154F99|b|OU outboxes
add|ob|alice OUTBOX rep|Object attribute|yes|alice OUTBOX|5C178D357D65A26FB8D73F589867535A|b|OU outboxes
add|b|charlie OUTBOX|b|OU outboxes
add|prop|outboxof=charlie|b|charlie OUTBOX
add|b|bob OUTBOX|b|OU outboxes
add|prop|outboxof=bob|b|bob OUTBOX
add|b|alice OUTBOX|b|OU outboxes
add|prop|outboxof=alice|b|alice OUTBOX
asg|s|11AE564E|b|katie OUTBOX
asg|u|alice|a|Doctor
asg|u|bob|a|Doctor
add|s|6DB75F58|oc|Ignored|a|Doctor
add|op|File write|s|6DB75F58
add|op|File read|s|6DB75F58
add|s|ADFA7E54|oc|Ignored|a|Doctor
add|op|File write|s|ADFA7E54
add|op|File read|s|ADFA7E54
add|s|53811547|oc|Ignored|a|Doctor
add|op|File write|s|53811547
add|op|File read|s|53811547
asg|a|Doctor|s|5C59BE03
add|s|4D87458D|oc|Ignored|a|Doctor
add|op|File write|s|4D87458D
add|op|File read|s|4D87458D
add|s|09B8AA6A|oc|Ignored|a|Doctor
add|op|File write|s|09B8AA6A
add|op|File read|s|09B8AA6A
add|s|AD72CEA8|oc|Ignored|a|Doctor
add|op|File read|s|AD72CEA8
asg|b|237D8FA7|b|AcctAddr
asg|s|CAF1CC9C|b|AcctAddr
asg|s|8B31D32C|b|AcctAddr
asg|b|30A44CB5|b|AcctSsn
asg|s|0AA431B8|b|AcctSsn
asg|s|3E5AF6F1|b|AcctSsn
asg|b|C9CFE6DE|b|AcctName
asg|s|EA3DBB84|b|AcctName
asg|s|F6CCB92B|b|AcctName
asg|b|FC15B612|b|AcctNum
asg|s|AD179DE1|b|AcctNum
asg|s|581A03EB|b|AcctNum
add|ob|E4B48FB1|File|no|ws-sys|C:\pmworkArea\E4B48FB1.rtf|b|PatTreatment
add|ob|E294203A|File|no|ws-sys|C:\pmworkArea\E294203A.rtf|b|PatTreatment
asg|s|6DB75F58|b|PatTreatment
asg|s|6523425D|b|PatTreatment
add|ob|53C1525D|File|no|ws-sys|C:\pmworkArea\53C1525D.rtf|b|PatDiag
add|ob|D9971E4A|File|no|ws-sys|C:\pmworkArea\D9971E4A.rtf|b|PatDiag
asg|s|ADFA7E54|b|PatDiag
asg|s|D36773F1|b|PatDiag
add|ob|33EAA2DF|File|no|ws-sys|C:\pmworkArea\33EAA2DF.rtf|b|PatSymptoms
add|ob|FB40D908|File|no|ws-sys|C:\pmworkArea\FB40D908.rtf|b|PatSymptoms
asg|s|C29A03C7|b|PatSymptoms
asg|s|4D87458D|b|PatSymptoms
add|ob|E9663596|File|no|ws-sys|C:\pmworkArea\E9663596.rtf|b|PatAllergies
add|ob|7002DA15|File|no|ws-sys|C:\pmworkArea\7002DA15.rtf|b|PatAllergies
asg|s|4E95A4D5|b|PatAllergies
asg|s|9CD521EB|b|PatAllergies
asg|s|09B8AA6A|b|PatAllergies
add|ob|6411940B|File|no|ws-sys|C:\pmworkArea\6411940B.doc|b|PatHistory
add|ob|FE2CA75B|File|no|ws-sys|C:\pmworkArea\FE2CA75B.doc|b|PatHistory
asg|s|53811547|b|PatHistory
asg|s|7F91A035|b|PatHistory
add|ob|39FA5BA8|File|no|ws-sys|C:\pmworkArea\39FA5BA8.bio|b|PatBio
add|ob|4902BD22|File|no|ws-sys|C:\pmworkArea\4902BD22.bio|b|PatBio
asg|s|0BC643D6|b|PatBio
add|ob|58423CA7|File|no|ws-sys|C:\pmworkArea\58423CA7.pid|b|PatId
add|ob|9435D63E|File|no|ws-sys|C:\pmworkArea\9435D63E.pid|b|PatId
asg|s|0B29B5B1|b|PatId
asg|s|233650FB|b|PatId
asg|s|AD72CEA8|b|PatId
asg|s|61538005|b|katie wINBOX
add|b|alice wINBOX|b|alice INBOX
add|prop|winboxof=alice|b|alice wINBOX
asg|s|1698910F|b|alice INBOX
add|b|charlie wINBOX|b|charlie INBOX
add|prop|winboxof=charlie|b|charlie wINBOX
asg|s|94E4DFD4|b|charlie INBOX
add|b|bob wINBOX|b|bob INBOX
add|prop|winboxof=bob|b|bob wINBOX
asg|s|F7D14F3F|b|bob INBOX
add|ob|Italian recipes|File|no|ws-sys|C:\pmworkArea\Italian recipes.rtf|b|Charlie recipes
add|ob|Chili recipes|File|no|ws-sys|C:\pmworkArea\Chili recipes.rtf|b|Charlie recipes
asg|b|9435D63E|b|8E094FF2
asg|b|4902BD22|b|8E094FF2
asg|b|7002DA15|b|8E094FF2
asg|b|FE2CA75B|b|8E094FF2
asg|b|FB40D908|b|8E094FF2
asg|b|D9971E4A|b|8E094FF2
asg|b|E294203A|b|8E094FF2
asg|b|58423CA7|b|8B54E24B
asg|b|39FA5BA8|b|8B54E24B
asg|b|E9663596|b|8B54E24B
asg|b|6411940B|b|8B54E24B
asg|b|33EAA2DF|b|8B54E24B
asg|b|53C1525D|b|8B54E24B
asg|b|E4B48FB1|b|8B54E24B
asg|s|B270223E|b|charlie OUTBOX rep
asg|s|504C2DFC|b|bob OUTBOX rep
asg|s|87499089|b|alice OUTBOX rep
asg|s|BCDE77D3|b|charlie OUTBOX
asg|s|19C91A87|b|bob OUTBOX
asg|s|BDB987A8|b|alice OUTBOX
asg|s|15068202|b|alice wINBOX
asg|s|D041A88C|b|charlie wINBOX
asg|s|B12DFFDD|b|bob wINBOX

add|app|Admin|@pm.install.dir@\dist\pm-admin-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.admin.PmAdmin|Admin >> |@pm.host.name@
add|app|Rich Text Editor|@pm.install.dir@\dist\pm-app-rtf-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.rtf.RTFEditor|RTF Editor >> |@pm.host.name@
add|app|Workflow Editor|@pm.install.dir@\dist\pm-app-wkf-pdf-@pm.version@.jar;@pm.install.dir@\dist\pm-app-pdf-view-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.workflow.WorkflowPDF|Workflow Editor >>|@pm.host.name@
add|app|PDF Viewer|@pm.install.dir@\dist\pm-app-pdf-view-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.pdfviewer.PDFViewer|PDF Viewer >> |@pm.host.name@
add|app|e-grant|@pm.install.dir@\dist\pm-app-grant-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.grantor.Grantor|e-grant >> |@pm.host.name@
add|app|Exporter|@pm.install.dir@\dist\pm-exporter-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.exporter.Exporter|Exporter >> |@pm.host.name@
add|app|Open Office|@pm.install.dir@\dist\pm-app-openoffice-@pm.version@.jar;@openoffice.runpath@|gov.nist.csd.pm.application.openoffice.OfficeLauncher|Open Office >>|@pm.host.name@
add|app|Microsoft Office Launcher|@pm.install.dir@\dist\pm-app-msoffice-@pm.version@.jar|gov.nist.csd.pm.application.office.MSOfficeLauncher|MS Office >>|@pm.host.name@
add|app|Med-Rec|@pm.install.dir@\dist\pm-app-medrec-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.medrec.MREditor|Med-Rec >>|@pm.host.name@
add|app|Acct-Rec|@pm.install.dir@\dist\pm-app-acctrec-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.acctrec.AcctEditor|Acct-Rec >>|@pm.host.name@
add|app|Workflow Old|@pm.install.dir@\dist\pm-app-wkf-old-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.oldworkflow.Wkflow|Workflow Old >>|@pm.host.name@


add|ks|@pm.install.dir@\keystores\superKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|super

add|ks|@pm.install.dir@\keystores\aliceKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|alice

add|ks|@pm.install.dir@\keystores\katieKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|katie

add|ks|@pm.install.dir@\keystores\daveKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|dave

add|ks|@pm.install.dir@\keystores\bobKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|bob

add|ks|@pm.install.dir@\keystores\charlieKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|charlie

add|ks|@pm.install.dir@\keystores\exporterKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|exporter

add|tpl|acctTpl|conts|AcctNum:AcctName:AcctSsn:AcctAddr
add|key|acctnum|tpl|acctTpl
add|tpl|mrecTpl|conts|PatId:PatBio:PatAllergies:PatHistory:PatSymptoms:PatDiag:PatTreatment
add|key|last name|tpl|mrecTpl
add|key|home phone|tpl|mrecTpl
add|key|ssn|tpl|mrecTpl
add|key|mrn|tpl|mrecTpl
add|tpl|acctTpl|b|12345678
add|comps|FC15B612:C9CFE6DE:30A44CB5:237D8FA7|b|12345678
add|key|acctnum=12345678|b|12345678
add|tpl|mrecTpl|b|8B54E24B
add|comps|58423CA7:39FA5BA8:E9663596:6411940B:33EAA2DF:53C1525D:E4B48FB1|b|8B54E24B
add|key|home phone=4321|b|8B54E24B
add|key|last name=Johnson|b|8B54E24B
add|key|ssn=123456789|b|8B54E24B
add|key|mrn=8C1AA3D245A9|b|8B54E24B
add|tpl|mrecTpl|b|8E094FF2
add|comps|9435D63E:4902BD22:7002DA15:FE2CA75B:FB40D908:D9971E4A:E294203A|b|8E094FF2
add|key|home phone=5678|b|8E094FF2
add|key|last name=Roberts|b|8E094FF2
add|key|ssn=124456789|b|8E094FF2
add|key|mrn=0DAB52862379|b|8E094FF2

add|prop|alice.signature.file|v|@pm.install.dir@\resources\signatures\alice.jpg
add|prop|bob.signature.file|v|@pm.install.dir@\resources\signatures\bob.jpg
add|prop|charlie.signature.file|v|@pm.install.dir@\resources\signatures\charlie.jpg
add|prop|dave.signature.file|v|@pm.install.dir@\resources\signatures\dave.jpg
add|prop|gavrila.signature.file|v|@pm.install.dir@\resources\signatures\gavrila.jpg
add|prop|katie.signature.file|v|@pm.install.dir@\resources\signatures\katie.jpg
