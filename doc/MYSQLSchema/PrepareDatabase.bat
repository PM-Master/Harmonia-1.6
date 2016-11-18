@echo on
cd c:/PM/doc/MYSQLSchema
type PolicyDB.sql auto_increment.sql PolicyDB_metadata.sql > PMSQL.sql
cd C:\Program Files\MySQL\MySQL Server 5.7\bin
mysql -u root -p < C:\PM\doc\MYSQLSchema\PMSQL.sql
echo Exit from MYSQL...
pause
