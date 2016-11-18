##############  Server Configuration Commands ##############
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
add|b|S_TS|p|MLS
add|ob|TS rep|Object attribute|no|TS|97946C24894C83121992A368042A3A0F|p|MLS
add|ob|S rep|Object attribute|no|S|176ED2D3E560F82E260CC207BDDFE447|p|MLS
add|a|Acct Mgr|p|DAC
add|a|Acct Repr|p|DAC
add|a|Nurse|p|DAC
add|a|Adm Clerk|p|DAC
add|a|DAC uattrs|p|DAC
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
add|prop|usersof=discretionary|a|DAC uattrs
add|ob|Acct Recs rep|Object attribute|yes|Acct Recs|4DD337BDB071C3682DF49C9329D54646|p|DAC
add|ob|Alice Med Records rep|Object attribute|yes|Alice Med Records|77482D7CEAEFF5284590C5D23C057897|p|DAC
add|ob|Bob Med Records rep|Object attribute|yes|Bob Med Records|8462AE7B73F55E8F2EDFDF9A3BD6B781|p|DAC
add|ob|Populated Forms rep|Object attribute|no|Populated Forms|655ADCAE8DF469B352351C42D5357EC6|p|DAC
add|ob|katie home rep|Object attribute|yes|katie home|C475E42A5BE362886D8FA48F572492D6|p|DAC
add|ob|inboxes rep|Object attribute|yes|inboxes|B9AD57D669853A054776C14036E60910|p|DAC
add|ob|charlie home rep|Object attribute|yes|charlie home|8E455CE65DBD68C06D91B31EBFF00CA1|p|DAC
add|ob|bob home rep|Object attribute|yes|bob home|CCB001DA9BD045C51295B6461576D9BE|p|DAC
add|ob|alice home rep|Object attribute|yes|alice home|A41943CB4069E871D69D1B4FB24E8868|p|DAC
add|ob|CMRecs rep|Object attribute|yes|CMRecs|47561254E70FAA7EFD2650E1AECC770A|p|DAC
add|b|Acct Recs|p|DAC
add|b|Today|p|DAC
add|b|Populated Forms|p|DAC
add|ob|DAC uattrs rep|User attribute|yes|DAC uattrs|9E6061CBD864A896CE5A642507F72AE1|p|DAC
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
add|b|Records|p|RBAC
add|prop|type=Records|b|Records
add|b|Acct Columns|p|RBAC
add|b|CMR Columns|p|RBAC
add|ob|Acct Columns rep|Object attribute|yes|Acct Columns|20A7462A510C7E61524C082B261B6BA4|p|RBAC
add|ob|CMR Columns rep|Object attribute|yes|CMR Columns|41C6FF29155D50CC28BC6B6A0C747E04|p|RBAC
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
add|ob|OU messages rep|Object attribute|yes|OU messages|B3BB895308E980DA3FEE4BF0E6F68F22|p|Confine
add|b|OU messages|p|Confine
add|a|Top secret|a|Secret
add|prop|correspondsto=TS|a|Top secret
add|u|bob|fn|Robert|a|Top Secret
add|eml|Robert|bob@nist.gov|email.nist.gov|email.nist.gov|nist|bob|u|bob
add|s|033A0D1C|oc|Ignored|a|Secret
add|op|Object attribute assign to|s|033A0D1C
add|op|Object attribute create object|s|033A0D1C
add|s|C2CF01CD|oc|Ignored|a|Secret
add|op|File read|s|C2CF01CD
asg|s|C2CF01CD|b|S
add|s|20DE6FEB|oc|Ignored|a|Secret
add|op|File write|s|20DE6FEB
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
add|u|dave|fn|David|a|Nurse
add|s|4EBE4892|oc|Ignored|a|Nurse
add|op|File read|s|4EBE4892
add|op|File write|s|4EBE4892
add|s|6BB7EBA7|oc|Ignored|a|Nurse
add|op|File read|s|6BB7EBA7
add|op|File write|s|6BB7EBA7
add|s|D7B13327|oc|Ignored|a|Nurse
add|op|File read|s|D7B13327
add|s|4D72BCE2|oc|Ignored|a|Nurse
add|op|File read|s|4D72BCE2
add|op|File write|s|4D72BCE2
add|s|6523425D|oc|Ignored|a|Nurse
add|op|File read|s|6523425D
add|s|D36773F1|oc|Ignored|a|Nurse
add|op|File read|s|D36773F1
add|s|7F91A035|oc|Ignored|a|Nurse
add|op|File read|s|7F91A035
asg|u|katie|a|Adm Clerk
add|s|830C202D|oc|Ignored|a|Adm Clerk
add|op|Object attribute create object|s|830C202D
add|op|Object attribute create object attribute|s|830C202D
add|s|FEBB2C43|oc|Ignored|a|Adm Clerk
add|op|Object attribute create object|s|FEBB2C43
add|op|Object attribute create object attribute|s|FEBB2C43
add|s|6121EE87|oc|Ignored|a|Adm Clerk
add|op|File write|s|6121EE87
add|s|02CDCAC2|oc|Ignored|a|Adm Clerk
add|op|Object attribute create object|s|02CDCAC2
add|op|Object attribute create object attribute|s|02CDCAC2
add|s|46A18983|oc|Ignored|a|Adm Clerk
add|op|File read|s|46A18983
add|op|File write|s|46A18983
add|s|56B1E529|oc|Ignored|a|Adm Clerk
add|op|Object attribute create object|s|56B1E529
add|op|Object attribute create object attribute|s|56B1E529
add|s|09C9B277|oc|Ignored|a|Adm Clerk
add|op|File read|s|09C9B277
add|op|File write|s|09C9B277
add|s|1DDA9933|oc|Ignored|a|Adm Clerk
add|op|File read|s|1DDA9933
add|op|File write|s|1DDA9933
add|s|C5F57D1C|oc|Ignored|a|Adm Clerk
add|op|Object attribute assign|s|C5F57D1C
add|s|AD27716D|oc|Ignored|a|Adm Clerk
add|op|Object attribute create object attribute|s|AD27716D
add|op|Object attribute assign to|s|AD27716D
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
add|b|dave INBOX|b|inboxes
add|prop|inboxof=dave|b|dave INBOX
add|b|dave wINBOX|b|dave INBOX
add|prop|winboxof=dave|b|dave wINBOX
add|s|87654321|oc|Ignored|a|David
add|op|File read|s|87654321
asg|s|87654321|b|dave INBOX
add|s|97654321|oc|Ignored|a|David
add|op|File write|s|97654321
asg|s|97654321|b|dave wINBOX
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
add|s|12345679|oc|Ignored|b|DAC uattrs rep
add|op|User assign|s|12345679
add|op|User attribute assign to operation set|s|12345679
add|s|15E69BE1|oc|Ignored|b|DAC uattrs rep
add|op|User assign|s|15E69BE1
add|op|User attribute assign to operation set|s|15E69BE1
add|s|EE568F88|oc|Ignored|b|DAC uattrs rep
add|op|User attribute assign to operation set|s|EE568F88
add|op|User assign|s|EE568F88
add|s|60507C01|oc|Ignored|b|DAC uattrs rep
add|op|User attribute assign to operation set|s|60507C01
add|op|User assign|s|60507C01
add|ob|katie OUTBOX rep|Object attribute|yes|katie OUTBOX|DFA7AFEA0C11710DA065F3A360530002|b|outboxes
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
asg|u|dave|a|Acquisition
add|s|D6F23181|oc|Ignored|a|Acquisition
add|op|File read|s|D6F23181
asg|u|katie|a|Secretary
add|s|D7048A1C|oc|Ignored|a|Secretary
add|op|File read|s|D7048A1C
add|op|File write|s|D7048A1C
add|a|Doctor|a|Intern
add|s|63C0F4E0|oc|Ignored|a|Intern
add|op|Object attribute assign to|s|63C0F4E0
add|op|Object attribute create object|s|63C0F4E0
add|s|17D90B49|oc|Ignored|a|Intern
add|op|File modify|s|17D90B49
add|op|File read|s|17D90B49
add|op|File write|s|17D90B49
add|s|945358F8|oc|Ignored|a|Intern
add|op|File read|s|945358F8
add|s|F14BB514|oc|Ignored|a|Intern
add|op|File read|s|F14BB514
add|s|011D8B80|oc|Ignored|a|Intern
add|op|Object attribute create object|s|011D8B80
add|op|Object attribute assign to|s|011D8B80
add|s|24214879|oc|Ignored|a|Intern
add|op|File read|s|24214879
add|s|690D3A30|oc|Ignored|a|Intern
add|op|Object attribute create object|s|690D3A30
add|op|Object attribute assign to|s|690D3A30
add|op|Object attribute assign|s|690D3A30
add|s|D1687D6A|oc|Ignored|a|Intern
add|op|File read|s|D1687D6A
add|s|C74AAC05|oc|Ignored|a|Intern
add|op|Object attribute create object|s|C74AAC05
add|op|Object attribute assign to|s|C74AAC05
asg|s|47CE9638|b|Acct Columns rep
asg|s|C5F57D1C|b|CMR Columns rep
add|b|AcctAddr|b|Acct Columns
add|b|AcctSsn|b|Acct Columns
add|b|AcctName|b|Acct Columns
add|b|AcctNum|b|Acct Columns
add|ob|PatSymptoms rep|Object attribute|no|PatSymptoms|A88CC4B41476659B594BEAC29FDCD130|b|CMR Columns
add|ob|PatAllergies rep|Object attribute|no|PatAllergies|279CD00D466424FE5F3B0101FFB80F1E|b|CMR Columns
add|ob|PatBio rep|Object attribute|no|PatBio|2C99E607EBA250C05DB0D2596BA267EB|b|CMR Columns
add|ob|PatId rep|Object attribute|no|PatId|2C7B611E6AA1F66EF474C76F9382C1D2|b|CMR Columns
add|ob|PatDrafts rep|Object attribute|yes|PatDrafts|BE5A9E75D98415A2F7F7C1EFCF0C0A5A|b|CMR Columns
add|ob|PatTreatment rep|Object attribute|no|PatTreatment|1FFD4BDD2546CEA8FB53558DF55363D0|b|CMR Columns
add|ob|PatDiag rep|Object attribute|no|PatDiag|6EBBF70A37DD42C4B8D9E6D401EA792F|b|CMR Columns
add|ob|PatHistory rep|Object attribute|no|PatHistory|A753D0570DCE8F1FD843E4613F47970D|b|CMR Columns
add|b|PatDrafts|b|CMR Columns
add|prop|prop=PatDrafts|b|PatDrafts
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
add|ob|poForm|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\poForm.wkf|b|Forms
asg|s|D7048A1C|b|Forms
add|ob|mrec33|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec33.rtf|b|Med Records
add|ob|mrec22|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec22.rtf|b|Med Records
add|ob|mrec11|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec11.rtf|b|Med Records
add|ob|mrec3|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec3.doc|b|Med Records
add|ob|mrec2|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec2.doc|b|Med Records
add|ob|mrec1|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec1.doc|b|Med Records
add|ob|mrec4|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec4.doc|b|Med Records
add|ob|mrec5|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\mrec5.rtf|b|Med Records
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
asg|u|alice|a|Secret
#asg|a|Top secret|s|650F246B
add|s|61FAF91D|oc|Ignored|a|Top secret
add|op|File write|s|61FAF91D
asg|a|Top secret|s|78E5ABCE
asg|b|mrec22|b|S
asg|b|mrec2|b|S
asg|b|mrec4|b|S
asg|b|mrec11|b|TS
asg|b|mrec1|b|TS
asg|s|61FAF91D|b|TS
asg|u|dave|a|David
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
asg|a|Katherine|s|12345679
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
add|s|BBD10825|oc|Ignored|a|Robert
add|op|Object attribute assign to|s|BBD10825
add|op|Object attribute create object|s|BBD10825
add|s|D827BFC6|oc|Ignored|a|Robert
add|op|File modify|s|D827BFC6
add|op|File read|s|D827BFC6
add|op|File write|s|D827BFC6
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
add|s|9079F508|oc|Ignored|a|Robert
add|op|File read|s|9079F508
add|s|79E12419|oc|Ignored|a|Robert
add|op|Object attribute create object|s|79E12419
add|op|Object attribute assign to|s|79E12419
add|s|BB2F4148|oc|Ignored|a|Robert
add|op|File read|s|BB2F4148
add|s|F8891E54|oc|Ignored|a|Robert
add|op|Object attribute create object|s|F8891E54
add|op|Object attribute assign to|s|F8891E54
add|s|14D0E012|oc|Ignored|a|Robert
add|op|File read|s|14D0E012
add|s|971B7214|oc|Ignored|a|Robert
add|op|Object attribute create object|s|971B7214
add|op|Object attribute assign to|s|971B7214
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
add|s|46C6EC2B|oc|Ignored|a|Alicia
add|op|File write|s|46C6EC2B
add|op|File read|s|46C6EC2B
add|op|File modify|s|46C6EC2B
add|s|05792340|oc|Ignored|a|Alicia
add|op|Object attribute create object|s|05792340
add|op|Object attribute assign to|s|05792340
add|s|08BAB037|oc|Ignored|a|Alicia
add|op|File read|s|08BAB037
add|s|B219B313|oc|Ignored|a|Alicia
add|op|Object attribute create object|s|B219B313
add|op|Object attribute assign to|s|B219B313
add|s|B4F11AFD|oc|Ignored|a|Alicia
add|op|File read|s|B4F11AFD
add|s|8DE0EB23|oc|Ignored|a|Alicia
add|op|Object attribute create object|s|8DE0EB23
add|op|Object attribute assign to|s|8DE0EB23
add|s|A4ADADDD|oc|Ignored|a|Alicia
add|op|File read|s|A4ADADDD
add|s|4FA145EA|oc|Ignored|a|Alicia
add|op|Object attribute create object|s|4FA145EA
add|op|Object attribute assign to|s|4FA145EA
add|u|exporter|fn|Exporter|a|Exporter
add|ob|FC15B612|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\FC15B612.txt|b|12345678
add|ob|C9CFE6DE|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\C9CFE6DE.txt|b|12345678
add|ob|30A44CB5|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\30A44CB5.txt|b|12345678
add|ob|237D8FA7|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\237D8FA7.txt|b|12345678
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
add|ob|prop1|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\prop1.rtf|b|Proposals
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
add|s|E664D534|oc|Ignored|a|Doctor
add|op|File read|s|E664D534
add|op|File write|s|E664D534
add|s|6D223F10|oc|Ignored|a|Doctor
add|op|File read|s|6D223F10
add|op|File write|s|6D223F10
add|s|AF698BEF|oc|Ignored|a|Doctor
add|op|File read|s|AF698BEF
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
asg|s|830C202D|b|PatSymptoms rep
asg|s|FEBB2C43|b|PatAllergies rep
asg|s|02CDCAC2|b|PatBio rep
asg|s|56B1E529|b|PatId rep
asg|s|63C0F4E0|b|PatDrafts rep
asg|s|011D8B80|b|PatTreatment rep
asg|s|690D3A30|b|PatDiag rep
asg|s|C74AAC05|b|PatHistory rep
add|b|PatHistoryDrafts|b|PatDrafts
add|prop|prop=PatHistoryDrafts|b|PatHistoryDrafts
add|b|PatDiagDrafts|b|PatDrafts
add|prop|prop=PatDiagDrafts|b|PatDiagDrafts
add|b|PatTreatmentDrafts|b|PatDrafts
add|prop|prop=PatTreatmentDrafts|b|PatTreatmentDrafts
asg|s|17D90B49|b|PatDrafts
add|ob|E4B48FB1|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\E4B48FB1.rtf|b|PatTreatment
add|ob|E294203A|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\E294203A.rtf|b|PatTreatment
asg|s|6DB75F58|b|PatTreatment
asg|s|6523425D|b|PatTreatment
asg|s|F14BB514|b|PatTreatment
add|ob|53C1525D|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\53C1525D.rtf|b|PatDiag
add|ob|D9971E4A|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\D9971E4A.rtf|b|PatDiag
asg|s|ADFA7E54|b|PatDiag
asg|s|D36773F1|b|PatDiag
asg|s|24214879|b|PatDiag
add|ob|d4cb3401|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\d4cb3401.rtf|b|PatSymptoms
add|ob|33EAA2DF|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\33EAA2DF.rtf|b|PatSymptoms
add|ob|FB40D908|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\FB40D908.rtf|b|PatSymptoms
asg|s|E664D534|b|PatSymptoms
asg|s|4EBE4892|b|PatSymptoms
add|ob|933161a3|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\933161a3.rtf|b|PatAllergies
add|ob|E9663596|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\E9663596.rtf|b|PatAllergies
add|ob|7002DA15|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\7002DA15.rtf|b|PatAllergies
asg|s|6D223F10|b|PatAllergies
asg|s|6121EE87|b|PatAllergies
asg|s|6BB7EBA7|b|PatAllergies
add|ob|6411940B|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\6411940B.doc|b|PatHistory
add|ob|FE2CA75B|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\FE2CA75B.doc|b|PatHistory
asg|s|53811547|b|PatHistory
asg|s|7F91A035|b|PatHistory
asg|s|D1687D6A|b|PatHistory
add|ob|9ce0521d|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\9ce0521d.bio|b|PatBio
add|ob|39FA5BA8|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\39FA5BA8.bio|b|PatBio
add|ob|4902BD22|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\4902BD22.bio|b|PatBio
asg|s|46A18983|b|PatBio
add|ob|91c1aa7f|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\91c1aa7f.pid|b|PatId
add|ob|58423CA7|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\58423CA7.pid|b|PatId
add|ob|9435D63E|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\9435D63E.pid|b|PatId
asg|s|AF698BEF|b|PatId
asg|s|09C9B277|b|PatId
asg|s|D7B13327|b|PatId
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
add|ob|Italian recipes|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\Italian recipes.rtf|b|Charlie recipes
add|ob|Chili recipes|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\Chili recipes.rtf|b|Charlie recipes
add|ob|8E094FF2-Drafts rep|Object attribute|yes|8E094FF2-Drafts|877E87232726C348B9F2C74A8BE49AA8|b|8E094FF2
add|ob|8E094FF2-Treatments rep|Object attribute|no|8E094FF2-Treatments|1662525EAF8045A81B21CFC5B3AE6B4F|b|8E094FF2
add|ob|8E094FF2-History rep|Object attribute|no|8E094FF2-History|45BFA9BE2A90CA90C8A261EC25D88F63|b|8E094FF2
add|ob|8E094FF2-Diagnoses rep|Object attribute|no|8E094FF2-Diagnoses|5CD87E2530C2B71DDB84E43F1723F37C|b|8E094FF2
add|b|8E094FF2-Drafts|b|8E094FF2
add|prop|Drafts=8E094FF2|b|8E094FF2-Drafts
asg|b|9435D63E|b|8E094FF2
asg|b|4902BD22|b|8E094FF2
asg|b|7002DA15|b|8E094FF2
asg|b|FE2CA75B|b|8E094FF2
asg|b|FB40D908|b|8E094FF2
asg|b|D9971E4A|b|8E094FF2
asg|b|E294203A|b|8E094FF2
add|b|8E094FF2-Treatments|b|8E094FF2
add|prop|Treatments=8E094FF2|b|8E094FF2-Treatments
add|b|8E094FF2-History|b|8E094FF2
add|prop|History=8E094FF2|b|8E094FF2-History
add|b|8E094FF2-Diagnoses|b|8E094FF2
add|prop|Diagnoses=8E094FF2|b|8E094FF2-Diagnoses
add|ob|8B54E24B-Drafts rep|Object attribute|yes|8B54E24B-Drafts|985F1545C0358ECFB33D3F78E5DE0650|b|8B54E24B
add|ob|8B54E24B-Diagnoses rep|Object attribute|yes|8B54E24B-Diagnoses|B7E56922D345BB6E90AB7654A518E536|b|8B54E24B
add|ob|8B54E24B-History rep|Object attribute|yes|8B54E24B-History|E41805708E6EE130F944DB91AB29FAA2|b|8B54E24B
add|ob|8B54E24B-Treatments rep|Object attribute|yes|8B54E24B-Treatments|915ACF01F2B58D6CE4AFDA177478188B|b|8B54E24B
asg|b|58423CA7|b|8B54E24B
asg|b|39FA5BA8|b|8B54E24B
asg|b|E9663596|b|8B54E24B
asg|b|6411940B|b|8B54E24B
asg|b|33EAA2DF|b|8B54E24B
asg|b|53C1525D|b|8B54E24B
asg|b|E4B48FB1|b|8B54E24B
add|b|8B54E24B-Treatments|b|8B54E24B
add|prop|Treatments=8B54E24B|b|8B54E24B-Treatments
add|b|8B54E24B-History|b|8B54E24B
add|prop|History=8B54E24B|b|8B54E24B-History
add|b|8B54E24B-Diagnoses|b|8B54E24B
add|prop|Diagnoses=8B54E24B|b|8B54E24B-Diagnoses
add|b|8B54E24B-Drafts|b|8B54E24B
add|prop|Drafts=8E094FF2|b|8B54E24B-Drafts
asg|s|B270223E|b|charlie OUTBOX rep
asg|s|504C2DFC|b|bob OUTBOX rep
asg|s|87499089|b|alice OUTBOX rep
asg|s|BCDE77D3|b|charlie OUTBOX
asg|s|19C91A87|b|bob OUTBOX
asg|s|BDB987A8|b|alice OUTBOX
asg|s|15068202|b|alice wINBOX
asg|s|D041A88C|b|charlie wINBOX
asg|s|B12DFFDD|b|bob wINBOX
asg|s|BBD10825|b|8E094FF2-Drafts rep
asg|s|79E12419|b|8E094FF2-Treatments rep
asg|s|F8891E54|b|8E094FF2-History rep
asg|s|971B7214|b|8E094FF2-Diagnoses rep
add|b|8E094FF2-DraftHistory|b|8E094FF2-Drafts
add|prop|DraftHistory=8E094FF2|b|8E094FF2-DraftHistory
add|b|8E094FF2-DraftDiagnoses|b|8E094FF2-Drafts
add|prop|DraftDiagnoses=8E094FF2|b|8E094FF2-DraftDiagnoses
add|b|8E094FF2-DraftTreatments|b|8E094FF2-Drafts
add|prop|DraftTreatments=8E094FF2|b|8E094FF2-DraftTreatments
asg|s|D827BFC6|b|8E094FF2-Drafts
asg|s|9079F508|b|8E094FF2-Treatments
asg|s|BB2F4148|b|8E094FF2-History
asg|s|14D0E012|b|8E094FF2-Diagnoses
asg|s|05792340|b|8B54E24B-Drafts rep
asg|s|B219B313|b|8B54E24B-Diagnoses rep
asg|s|8DE0EB23|b|8B54E24B-History rep
asg|s|4FA145EA|b|8B54E24B-Treatments rep
asg|s|A4ADADDD|b|8B54E24B-Treatments
asg|s|B4F11AFD|b|8B54E24B-History
asg|s|08BAB037|b|8B54E24B-Diagnoses
add|b|8B54E24B-DraftTreatments|b|8B54E24B-Drafts
add|prop|DraftTreatments=8B54E24B|b|8B54E24B-DraftTreatments
add|b|8B54E24B-DraftHistory|b|8B54E24B-Drafts
add|prop|DraftHistory=8B54E24B|b|8B54E24B-DraftHistory
add|b|8B54E24B-DraftDiagnoses|b|8B54E24B-Drafts
add|prop|DraftDiagnoses=8B54E24B|b|8B54E24B-DraftDiagnoses
asg|s|46C6EC2B|b|8B54E24B-Drafts
add|tpl|mrecTpl|conts|PatId:PatBio:PatAllergies:PatHistory:PatSymptoms:PatDiag:PatTreatment
add|key|last name|tpl|mrecTpl
add|key|home phone|tpl|mrecTpl
add|key|ssn|tpl|mrecTpl
add|key|mrn|tpl|mrecTpl
add|tpl|acctTpl|conts|AcctNum:AcctName:AcctSsn:AcctAddr
add|key|acctnum|tpl|acctTpl
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
add|tpl|acctTpl|b|12345678

add|p|emp_rec|c|PM
add|b|emp_rec|p|emp_rec
add|b|employees|b|emp_rec
add|b|employees_name|b|employees
add|b|employees_ssn|b|employees
add|b|employees_phone|b|employees
add|b|employees_salary|b|employees
add|b|Grp1 Records|b|emp_rec
add|b|Grp2 Records|b|emp_rec
add|b|HR Records|b|emp_rec
add|ob|emp_rec rep|Object attribute|yes|emp_rec|Ignored|p|emp_rec
add|a|emp_rec admins|p|emp_rec
asg|u|katie|a|emp_rec admins
add|a|Grp1|p|emp_rec
add|a|Grp2|p|emp_rec
add|a|Grp1Mgr|a|Grp1
add|a|Grp2Mgr|a|Grp2
add|a|HR|p|emp_rec
asg|u|bob|a|Grp1Mgr
asg|u|alice|a|Grp2Mgr
asg|u|dave|a|Grp1
asg|u|charlie|a|Grp2
asg|u|katie|a|HR
add|s|s_emp_rec|oc|Ignored|a|emp_rec admins
add|op|*|s|s_emp_rec
asg|s|s_emp_rec|b|emp_rec rep
add|s|s_grp1|oc|Ignored|a|Grp1
add|op|File read|s|s_grp1
add|op|File write|s|s_grp1
asg|s|s_grp1|b|Grp1 Records
add|s|s_grp2|oc|Ignored|a|Grp2
add|op|File read|s|s_grp2
add|op|File write|s|s_grp2
asg|s|s_grp2|b|Grp2 Records
add|s|s_emp_rec_admins|oc|Ignored|a|emp_rec admins
add|op|File read|s|s_emp_rec_admins
add|op|File write|s|s_emp_rec_admins
asg|s|s_emp_rec_admins|b|emp_rec

add|s|s_name|oc|Ignored|b|employees_name
add|op|File read|s|s_name
asg|a|Grp1|s|s_name

add|s|s_name1|oc|Ignored|b|employees_name
add|op|File read|s|s_name1
asg|a|Grp2|s|s_name1

add|s|s_phone|oc|Ignored|b|employees_phone
add|op|File read|s|s_phone
asg|a|Grp1|s|s_phone

add|s|s_phone1|oc|Ignored|b|employees_phone
add|op|File read|s|s_phone1
asg|a|Grp2|s|s_phone1

add|s|s_salary|oc|Ignored|b|employees_salary
add|op|File read|s|s_salary
asg|a|Grp1|s|s_salary

add|s|s_salary1|oc|Ignored|b|employees_salary
add|op|File read|s|s_salary1
asg|a|Grp2|s|s_salary1

add|s|s_ssn|oc|Ignored|b|employees_ssn
add|op|File read|s|s_ssn
asg|a|Grp1|s|s_ssn

add|s|s_ssn1|oc|Ignored|b|employees_ssn
add|op|File read|s|s_ssn1
asg|a|Grp2|s|s_ssn1

add|tpl|employees|conts|employees_ssn:employees_name:employees_phone:employees_salary
add|key|employees_phone|tpl|employees

#emp_rec Records
#Alice
add|b|cdad6137-dc30-481b-9948-741133d8b2d7|b|Grp2 Records
add|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_name|b|cdad6137-dc30-481b-9948-741133d8b2d7
add|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_ssn|b|cdad6137-dc30-481b-9948-741133d8b2d7
add|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_phone|b|cdad6137-dc30-481b-9948-741133d8b2d7
add|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_salary|b|cdad6137-dc30-481b-9948-741133d8b2d7
add|ob|4ab63bb5|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\4ab63bb5.doc|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_name
add|ob|d16173ff|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\d16173ff.doc|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_ssn
add|ob|bd15c30a|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bd15c30a.doc|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_phone
add|ob|bd674751|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bd674751.doc|b|cdad6137-dc30-481b-9948-741133d8b2d7_employees_salary
asg|o|4ab63bb5|b|employees_name
asg|o|d16173ff|b|employees_ssn
asg|o|bd15c30a|b|employees_phone
asg|o|bd674751|b|employees_salary
add|tpl|employees|b|cdad6137-dc30-481b-9948-741133d8b2d7
add|comps|cdad6137-dc30-481b-9948-741133d8b2d7_employees_name:cdad6137-dc30-481b-9948-741133d8b2d7_employees_ssn:cdad6137-dc30-481b-9948-741133d8b2d7_employees_phone:cdad6137-dc30-481b-9948-741133d8b2d7_employees_salary|b|cdad6137-dc30-481b-9948-741133d8b2d7

#Bob
add|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0|b|Grp1 Records
add|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_name|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0
add|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_ssn|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0
add|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_phone|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0
add|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_salary|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0
add|ob|e0c10b05|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\e0c10b05.doc|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_name
add|ob|ffe5d5a2|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\ffe5d5a2.doc|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_ssn
add|ob|0da6e166|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\0da6e166.doc|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_phone
add|ob|84f4b677|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\84f4b677.doc|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_salary
asg|o|e0c10b05|b|employees_name
asg|o|ffe5d5a2|b|employees_ssn
asg|o|0da6e166|b|employees_phone
asg|o|84f4b677|b|employees_salary
add|tpl|employees|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0
add|comps|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_name:0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_ssn:0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_phone:0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_salary|b|0d732faf-0c99-4341-a2ed-b7fb64b57ef0

#Katie
add|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1|b|HR Records
add|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_name|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1
add|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_ssn|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1
add|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_phone|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1
add|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_salary|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1
add|ob|bf9b7fb5|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bf9b7fb5.doc|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_name
add|ob|5b3fd73c|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\5b3fd73c.doc|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_ssn
add|ob|b26c5cf7|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\b26c5cf7.doc|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_phone
add|ob|1fd50aa1|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bd674751.doc|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_salary
asg|o|bf9b7fb5|b|employees_name
asg|o|5b3fd73c|b|employees_ssn
asg|o|b26c5cf7|b|employees_phone
asg|o|1fd50aa1|b|employees_salary
add|tpl|employees|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1
add|comps|e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_name:e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_ssn:e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_phone:e9d29540-c7e3-455f-8a3c-7f60d9b913b1_employees_salary|b|e9d29540-c7e3-455f-8a3c-7f60d9b913b1

#Dave
add|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80|b|Grp1 Records
add|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_name|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80
add|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_ssn|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80
add|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_phone|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80
add|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_salary|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80
add|ob|074a6610|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\074a6610.doc|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_ssn
add|ob|7097ddf8|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\7097ddf8.doc|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_salary
add|ob|b27dc7e0|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\b27dc7e0.doc|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_phone
add|ob|bf08b418|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bf08b418.doc|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_name
asg|o|bf08b418|b|employees_name
asg|o|074a6610|b|employees_ssn
asg|o|b27dc7e0|b|employees_phone
asg|o|7097ddf8|b|employees_salary
add|tpl|employees|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80
add|comps|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_name:ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_ssn:ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_phone:ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_salary|b|ab514faf-2626-45c6-b2c2-4a1f350f5c80

#Charlie
add|b|a99f6b4b-e658-4998-a81b-33f86dd713d8|b|Grp2 Records
add|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_name|b|a99f6b4b-e658-4998-a81b-33f86dd713d8
add|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_ssn|b|a99f6b4b-e658-4998-a81b-33f86dd713d8
add|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_phone|b|a99f6b4b-e658-4998-a81b-33f86dd713d8
add|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_salary|b|a99f6b4b-e658-4998-a81b-33f86dd713d8
add|ob|e0bfd15b|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\e0bfd15b.doc|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_ssn
add|ob|b008ceeb|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\b008ceeb.doc|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_salary
add|ob|8b1b37ff|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\8b1b37ff.doc|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_phone
add|ob|f50e27f3|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\f50e27f3.doc|b|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_name
asg|o|f50e27f3|b|employees_name
asg|o|e0bfd15b|b|employees_ssn
asg|o|8b1b37ff|b|employees_phone
asg|o|b008ceeb|b|employees_salary
add|tpl|employees|b|a99f6b4b-e658-4998-a81b-33f86dd713d8
add|comps|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_name:a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_ssn:a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_phone:a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_salary|b|a99f6b4b-e658-4998-a81b-33f86dd713d8


#end emp_rec records

#add|deny|<deny name>|<deny type>|<user or attr>|<user or attr name>|<is intersection>
#add|op|<operation>|deny|<deny name>
#add|b|<oattr name>|<deny>|<deny name>
#add|cb|<oattr name>|<deny>|<deny name>

#bob denies

add|deny|deny-bob-employees_ssn|user id|u|bob|yes
add|b|employees_ssn|deny|deny-bob-employees_ssn
add|cb|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_ssn|deny|deny-bob-employees_ssn
add|op|File read|deny|deny-bob-employees_ssn
add|op|File write|deny|deny-bob-employees_ssn

add|deny|deny-bob-write-employees_salary|user id|u|bob|yes
add|b|employees_salary|deny|deny-bob-write-employees_salary
add|op|File write|deny|deny-bob-write-employees_salary

add|deny|deny-bob-read-grp2-employees_salary|user id|u|bob|yes
add|b|employees_salary|deny|deny-bob-read-grp2-employees_salary
add|b|Grp2 Records|deny|deny-bob-read-grp2-employees_salary
add|op|File read|deny|deny-bob-read-grp2-employees_salary

add|deny|deny-bob-read-hr-employees_salary|user id|u|bob|yes
add|b|employees_salary|deny|deny-bob-read-hr-employees_salary
add|b|HR Records|deny|deny-bob-read-hr-employees_salary
add|op|File read|deny|deny-bob-read-hr-employees_salary

add|deny|deny-bob-employees_name|user id|u|bob|yes
add|b|employees_name|deny|deny-bob-employees_name
add|cb|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_name|deny|deny-bob-employees_name
add|op|File write|deny|deny-bob-employees_name

add|deny|deny-bob-employees_phone|user id|u|bob|yes
add|b|employees_phone|deny|deny-bob-employees_phone
add|cb|0d732faf-0c99-4341-a2ed-b7fb64b57ef0_employees_phone|deny|deny-bob-employees_phone
add|op|File write|deny|deny-bob-employees_phone

#alice denies
add|deny|deny-alice-employees_ssn|user id|u|alice|yes
add|b|employees_ssn|deny|deny-alice-employees_ssn
add|cb|cdad6137-dc30-481b-9948-741133d8b2d7_employees_ssn|deny|deny-alice-employees_ssn
add|op|File read|deny|deny-alice-employees_ssn
add|op|File write|deny|deny-alice-employees_ssn

add|deny|deny-alice-write-employees_salary|user id|u|alice|yes
add|b|employees_salary|deny|deny-alice-write-employees_salary
add|op|File write|deny|deny-alice-write-employees_salary

add|deny|deny-alice-read-Grp1-employees_salary|user id|u|alice|yes
add|b|employees_salary|deny|deny-alice-read-Grp1-employees_salary
add|b|Grp1 Records|deny|deny-alice-read-Grp1-employees_salary
add|op|File read|deny|deny-alice-read-Grp1-employees_salary

add|deny|deny-alice-read-hr-employees_salary|user id|u|alice|yes
add|b|employees_salary|deny|deny-alice-read-hr-employees_salary
add|b|HR Records|deny|deny-alice-read-hr-employees_salary
add|op|File read|deny|deny-alice-read-hr-employees_salary

add|deny|deny-alice-employees_name|user id|u|alice|yes
add|b|employees_name|deny|deny-alice-employees_name
add|cb|cdad6137-dc30-481b-9948-741133d8b2d7_employees_name|deny|deny-alice-employees_name
add|op|File write|deny|deny-alice-employees_name

add|deny|deny-alice-employees_phone|user id|u|alice|yes
add|b|employees_phone|deny|deny-alice-employees_phone
add|cb|cdad6137-dc30-481b-9948-741133d8b2d7_employees_phone|deny|deny-alice-employees_phone
add|op|File write|deny|deny-alice-employees_phone

#Dave denies
add|deny|deny-dave-employees_ssn|user id|u|dave|yes
add|b|employees_ssn|deny|deny-dave-employees_ssn
add|cb|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_ssn|deny|deny-dave-employees_ssn
add|op|File read|deny|deny-dave-employees_ssn
add|op|File write|deny|deny-dave-employees_ssn

add|deny|deny-dave-rw-salary|user id|u|dave|yes
add|b|employees_salary|deny|deny-dave-rw-salary|user
add|cb|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_salary|deny|deny-dave-rw-salary
add|op|File read|deny|deny-dave-rw-salary

add|deny|deny-dave-write-employees_salary|user id|u|dave|yes
add|b|employees_salary|deny|deny-dave-write-employees_salary
add|op|File write|deny|deny-dave-write-employees_salary

add|deny|deny-dave-employees_name|user id|u|dave|yes
add|b|employees_name|deny|deny-dave-employees_name
add|cb|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_name|deny|deny-dave-employees_name
add|op|File write|deny|deny-dave-employees_name

add|deny|deny-dave-employees_phone|user id|u|dave|yes
add|b|employees_phone|deny|deny-dave-employees_phone
add|cb|ab514faf-2626-45c6-b2c2-4a1f350f5c80_employees_phone|deny|deny-dave-employees_phone
add|op|File write|deny|deny-dave-employees_phone

#Charlie denies
add|deny|deny-charlie-employees_ssn|user id|u|charlie|yes
add|b|employees_ssn|deny|deny-charlie-employees_ssn
add|cb|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_ssn|deny|deny-charlie-employees_ssn
add|op|File read|deny|deny-charlie-employees_ssn
add|op|File write|deny|deny-charlie-employees_ssn

add|deny|deny-charlie-rw-salary|user id|u|charlie|yes
add|b|employees_salary|deny|deny-charlie-rw-salary|user
add|cb|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_salary|deny|deny-charlie-rw-salary
add|op|File read|deny|deny-charlie-rw-salary

add|deny|deny-charlie-write-employees_salary|user id|u|charlie|yes
add|b|employees_salary|deny|deny-charlie-write-employees_salary
add|op|File write|deny|deny-charlie-write-employees_salary

add|deny|deny-charlie-employees_name|user id|u|charlie|yes
add|b|employees_name|deny|deny-charlie-employees_name
add|cb|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_name|deny|deny-charlie-employees_name
add|op|File write|deny|deny-charlie-employees_name

add|deny|deny-charlie-employees_phone|user id|u|charlie|yes
add|b|employees_phone|deny|deny-charlie-employees_phone
add|cb|a99f6b4b-e658-4998-a81b-33f86dd713d8_employees_phone|deny|deny-charlie-employees_phone
add|op|File write|deny|deny-charlie-employees_phone

#med-rec objects
add|ob|c9af7065|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\c9af7065.rtf|b|PatHistory
add|ob|4e2609b2|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\4e2609b2.rtf|b|PatDiag
add|ob|fc13bfab|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\fc13bfab.rtf|b|PatDiag
add|ob|0859b57e|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\0859b57e.rtf|b|PatDiag
add|ob|e1cb9d1c|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\e1cb9d1c.rtf|b|PatTreatment
add|ob|bfe20890|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\bfe20890.rtf|b|PatTreatment
add|ob|975555c4|File|no|[SERVER_COMPUTER_NAME]|C:\PMWorkArea\975555c4.rtf|b|PatTreatment

asg|b|4e2609b2|b|8E094FF2-Diagnoses
asg|b|4e2609b2|b|TS
asg|b|fc13bfab|b|8E094FF2-Diagnoses
asg|b|0859b57e|b|8E094FF2-Diagnoses
asg|b|c9af7065|b|8E094FF2-History
asg|b|e1cb9d1c|b|8E094FF2-Treatments
asg|b|bfe20890|b|8E094FF2-Treatments
asg|b|975555c4|b|8E094FF2-Treatments



add|comps|FC15B612:C9CFE6DE:30A44CB5:237D8FA7|b|12345678
add|key|acctnum=12345678|b|12345678

#add|prop|alice.signature.file|v|C:\PM\resources\signatures\alice.jpg
#add|prop|bob.signature.file|v|C:\PM\resources\signatures\bob.jpg
#add|prop|charlie.signature.file|v|C:\PM\resources\signatures\charlie.jpg
#add|prop|dave.signature.file|v|C:\PM\resources\signatures\dave.jpg
#add|prop|gavrila.signature.file|v|C:\PM\resources\signatures\gavrila.jpg
#add|prop|katie.signature.file|v|C:\PM\resources\signatures\katie.jpg

##############  Client Configuration Commands ##############
add|app|Admin|C:\PM\dist\pm-admin-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.admin.PmAdmin|Admin >> |[SERVER_COMPUTER_NAME]
add|app|Rich Text Editor|C:\PM\dist\pm-app-rtf-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.rtf.RTFEditor|RTF Editor >> |[SERVER_COMPUTER_NAME]
add|app|Workflow Editor|C:\PM\dist\pm-app-wkf-pdf-1.6.jar;C:\PM\dist\pm-app-pdf-view-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.workflow.WorkflowPDF|Workflow Editor >>|[SERVER_COMPUTER_NAME]
add|app|PDF Viewer|C:\PM\dist\pm-app-pdf-view-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.pdfviewer.PDFViewer|PDF Viewer >> |[SERVER_COMPUTER_NAME]
add|app|e-grant|C:\PM\dist\pm-app-grant-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.grantor.Grantor|e-grant >> |[SERVER_COMPUTER_NAME]
add|app|Exporter|C:\PM\dist\pm-exporter-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.exporter.Exporter|Exporter >> |[SERVER_COMPUTER_NAME]
add|app|Open Office|C:\PM\dist\pm-app-openoffice-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*;C:\Program Files (x86)\OpenOffice 4\program\classes\unoil.jar;C:\Program Files (x86)\OpenOffice 4\program\classes\juh.jar;C:\Program Files (x86)\OpenOffice 4\program\classes\jurt.jar;C:\Program Files (x86)\OpenOffice 4\program\classes\ridl.jar;C:\Program Files (x86)\OpenOffice 4\program|gov.nist.csd.pm.application.openoffice.OfficeLauncher|Open Office >>|[SERVER_COMPUTER_NAME]
add|app|Microsoft Office Launcher|C:\PM\dist\pm-app-msoffice-1.6.jar|gov.nist.csd.pm.application.office.MSOfficeLauncher|MS Office >>|[SERVER_COMPUTER_NAME]
add|app|Med-Rec|C:\PM\dist\pm-app-medrec-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.medrec.MREditor|Med-Rec >>|[SERVER_COMPUTER_NAME]
add|app|Acct-Rec|C:\PM\dist\pm-app-acctrec-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.acctrec.AcctEditor|Acct-Rec >>|[SERVER_COMPUTER_NAME]
add|app|Workflow Old|C:\PM\dist\pm-app-wkf-old-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.oldworkflow.Wkflow|Workflow Old >>|[SERVER_COMPUTER_NAME]
add|app|Schema Builder|C:\PM\dist\pm-app-schemabuilder-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.schema.builder.SchemaBuilder3|SB>>|[SERVER_COMPUTER_NAME]
add|app|Employee Record|C:\PM\dist\pm-app-emprec-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.appeditor.EmployeeRecord|ER>>|[SERVER_COMPUTER_NAME]
add|app|Table Editor|C:\PM\dist\pm-app-tableeditor-1.6.jar;C:\PM\dist\pm-commons-1.6.jar;C:\PM\lib\*|gov.nist.csd.pm.application.schema.tableeditor.TableEditor|TE>>|[SERVER_COMPUTER_NAME]

add|ks|C:\PM\keystores\superKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|super

add|ks|C:\PM\keystores\aliceKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|alice

add|ks|C:\PM\keystores\katieKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|katie

add|ks|C:\PM\keystores\daveKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|dave

add|ks|C:\PM\keystores\bobKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|bob

add|ks|C:\PM\keystores\charlieKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|charlie

add|ks|C:\PM\keystores\exporterKeystore|C:\PM\keystores\clientTruststore|h|[SERVER_COMPUTER_NAME]|u|exporter
