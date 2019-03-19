REM  -----------------------------------------------------------
REM  PM Run Server (Same as old PmServer/server.bat)
REM  Steve Quirolgico
REM  06/07/08
REM  -----------------------------------------------------------


REM ------------------------------------------------------------
REM     MODIFY THE FOLLOWING PATHS FOR YOUR ENVIRONMENT!
REM ------------------------------------------------------------



set PM_VERSION=1.6

set PM_ROOT=C:\PM

set MY_KEYSTORE=pmserverKeystore

set MY_TRUSTSTORE=pmserverTruststore

set ENGINE_PORT=8080

set JAVA_JRE=%JAVA_HOME%

TITLE Policy Machine Server


REM ------------------------------------------------------------
REM       DO NOT MODIFY ANYTHING BELOW THIS LINE!
REM ------------------------------------------------------------



set KEYSTORES=%PM_ROOT%\keystores

set MY_KEYSTORE_PATH=%KEYSTORES%\%MY_KEYSTORE%

set MY_TRUSTSTORE_PATH=%KEYSTORES%\%MY_TRUSTSTORE%

set JAVA_BIN=%JAVA_JRE%\bin

set JAVA_LIB=%JAVA_JRE%\lib

set JAVA_JARS=%JAVA_LIB%\rt.jar;%JAVA_LIB%\jsse.jar

set PM_SERVER_JAR=%PM_ROOT%\dist\pm-server-%PM_VERSION%.jar

set PM_DATASTORE=SQL

set CLASSPATH=%PM_SERVER_JAR%;%PM_ROOT%\dist\pm-commons-%PM_VERSION%.jar;%PM_ROOT%\lib\*

set WAIT_FOR_DEBUGGER=n
set DEBUG_PORT=8003
set SERVER_MODE=y

set DEBUG_ARGS=-Xdebug -agentlib:jdwp=transport=dt_socket,suspend=%WAIT_FOR_DEBUGGER%,server=%SERVER_MODE%,address=%DEBUG_PORT%

echo Debug ARGS: %DEBUG_ARGS%

"%JAVA_BIN%\java" -cp "%CLASSPATH%" -Djavax.net.ssl.keyStore=%MY_KEYSTORE_PATH% -Djavax.net.ssl.trustStore=%MY_TRUSTSTORE_PATH% gov.nist.csd.pm.server.PmEngine -engineport %ENGINE_PORT% -datastore %PM_DATASTORE% -debug


