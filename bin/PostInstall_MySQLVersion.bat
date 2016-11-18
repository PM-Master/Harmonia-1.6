REM ----------- server preInstallation script (for MySQL version)

echo off
SETX JAVA_HOME "C:\Program Files (x86)\Java\jdk1.6.0_45"
set PM_ROOT=C:\PM

REM -- replace DOMAIN with domain --- fart C:\PM\doc\ADSchema\*.ldf [DOMAIN] %USERDNSDOMAIN%
set InFile1=%PM_ROOT%\conf\PMServerConfiguration.pm
set InFile2=%PM_ROOT%\conf\PMClientConfiguration.pm
set InFile3=%PM_ROOT%\doc\MYSQLSchema\PolicyDB_metadata.sql

%PM_ROOT%\bin\stringUtil %InFile1% [SERVER_COMPUTER_NAME] %computername%
%PM_ROOT%\bin\stringUtil %InFile2% [CLIENT_COMPUTER_NAME] %computername%
%PM_ROOT%\bin\stringUtil %InFile3% SERVER_COMPUTER_NAME %computername%
