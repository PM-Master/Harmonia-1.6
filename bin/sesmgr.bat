REM  -----------------------------------------------------------
REM  PM Run User (same as old Pmuser/sesmgrdbg.bat)
REM  Steve Quirolgico
REM  06/07/08
REM  -----------------------------------------------------------


REM ------------------------------------------------------------
REM     MODIFY THE FOLLOWING PATHS FOR YOUR ENVIRONMENT!
REM ------------------------------------------------------------



set PM_VERSION=1.6

set PM_ROOT=C:\PM

set MY_KEYSTORE=%computername%Keystore

set MY_KEYSTORE_PASSWORD=aaaaaa

set MY_TRUSTSTORE=pmserverTruststore

set SIM_PORT=8085

set EX_PORT=8086

set JAVA_JRE=%JAVA_HOME%

TITLE Policy Machine Session Manager


REM ------------------------------------------------------------
REM       DO NOT MODIFY ANYTHING BELOW THIS LINE!
REM ------------------------------------------------------------



set KEYSTORES=%PM_ROOT%\keystores

set MY_KEYSTORE_PATH=%KEYSTORES%\%MY_KEYSTORE%

set MY_TRUSTSTORE_PATH=%KEYSTORES%\%MY_TRUSTSTORE%

set JAVA_BIN=%JAVA_JRE%\bin

set JAVA_LIB=%JAVA_JRE%\lib

set CLASSPATH=%PM_ROOT%\dist\pm-user-%PM_VERSION%.jar;%PM_ROOT%\dist\pm-exporter-%PM_VERSION%.jar;%PM_ROOT%\dist\pm-commons-%PM_VERSION%.jar;%PM_ROOT%\lib\*

"%JAVA_BIN%\java" -cp "%CLASSPATH%" -Djavax.net.ssl.keyStore=%MY_KEYSTORE_PATH% -Djavax.net.ssl.keyStorePassword=%MY_KEYSTORE_PASSWORD% -Djavax.net.ssl.trustStore=%MY_TRUSTSTORE_PATH% gov.nist.csd.pm.user.SessionManager -simport %SIM_PORT% -export %EX_PORT% -debug
