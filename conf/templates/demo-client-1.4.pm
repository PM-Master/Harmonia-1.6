add|app|Admin|@pm.install.dir@\dist\pm-admin-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.admin.PmAdmin|Admin >> |@pm.host.name@
add|app|Rich Text Editor|@pm.install.dir@\dist\pm-app-rtf-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.rtf.RTFEditor|RTF Editor >> |@pm.host.name@
add|app|Workflow Editor|@pm.install.dir@\dist\pm-app-wkf-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.workflow.Wkflow|Workflow Editor >>|@pm.host.name@
add|app|e-grant|@pm.install.dir@\dist\pm-app-grant-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.grantor.Grantor|e-grant >> |@pm.host.name@
add|app|Exporter|@pm.install.dir@\dist\pm-exporter-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.exporter.Exporter|Exporter >> |@pm.host.name@
add|app|Open Office|@pm.install.dir@\dist\pm-app-openoffice-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.openoffice.OfficeLauncher|Open Office >>|@pm.host.name@
add|app|Microsoft Office Launcher|@pm.install.dir@\dist\pm-app-msoffice-@pm.version@.jar|gov.nist.csd.pm.application.office.MSOfficeLauncher|MS Office >>|@pm.host.name@
add|app|Med-Rec|@pm.install.dir@\dist\pm-app-medrec-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.medrec.MREditor|Med-Rec >>|@pm.host.name@
add|app|Acct-Rec|@pm.install.dir@\dist\pm-app-acctrec-@pm.version@.jar;@jar.dependencies@|gov.nist.csd.pm.application.acctrec.AcctEditor|Acct-Rec >>|@pm.host.name@


add|ks|@pm.install.dir@\keystores\superKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|super

add|ks|@pm.install.dir@\keystores\aliceKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|alice

add|ks|@pm.install.dir@\keystores\katieKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|katie

add|ks|@pm.install.dir@\keystores\daveKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|dave

add|ks|@pm.install.dir@\keystores\bobKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|bob

add|ks|@pm.install.dir@\keystores\charlieKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|charlie

add|ks|@pm.install.dir@\keystores\exporterKeystore|@pm.install.dir@\keystores\clientTruststore|h|@pm.host.name@|u|exporter