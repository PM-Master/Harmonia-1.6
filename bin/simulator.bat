REM  -----------------------------------------------------------
REM  PM Run Ksim (same as old PmKsim/simulatordbg.bat)
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

set ENGINE_HOST=%computername%

set ENGINE_PORT=8080

set SIM_PORT=8085

set JAVA_JRE=%JAVA_HOME%

TITLE Kernel Simulator


REM ------------------------------------------------------------
REM       DO NOT MODIFY ANYTHING BELOW THIS LINE!
REM ------------------------------------------------------------



set KEYSTORES=%PM_ROOT%\keystores

set MY_KEYSTORE_PATH=%KEYSTORES%\%MY_KEYSTORE%

set MY_TRUSTSTORE_PATH=%KEYSTORES%\%MY_TRUSTSTORE%

set JAVA_BIN=%JAVA_JRE%\bin

set JAVA_LIB=%JAVA_JRE%\lib

set JAVA_JARS=%JAVA_LIB%\rt.jar;%JAVA_LIB%\jsse.jar

set PM_JAR=%PM_ROOT%\dist\pm-ksim-%PM_VERSION%.jar

rem set CLASSPATH=%PM_JAR%;%JAVA_JARS%

set CLASSPATH=%PM_JAR%;%PM_ROOT%\dist\pm-commons-%PM_VERSION%.jar;%PM_ROOT%\lib\*

set WAIT_FOR_DEBUGGER=n

set DEBUG_PORT=8004

rem Remove the comment below to activate remote debugging of the simulator.

rem set DEBUG_ARGS=-Xdebug -agentlib:jdwp=transport=dt_socket,suspend=%WAIT_FOR_DEBUGGER%,server=y,address=%DEBUG_PORT%

echo Debug ARGS: %DEBUG_ARGS%

"%JAVA_BIN%\java" -cp "%CLASSPATH%" -Djavax.net.ssl.keyStore=%MY_KEYSTORE_PATH% -Djavax.net.ssl.keyStorePassword=%MY_KEYSTORE_PASSWORD% -Djavax.net.ssl.trustStore=%MY_TRUSTSTORE_PATH% gov.nist.csd.pm.ksim.KernelSimulator -enginehost %ENGINE_HOST% -engineport %ENGINE_PORT% -simport %SIM_PORT% -debug
