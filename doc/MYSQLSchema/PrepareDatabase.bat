cd c:/PM/doc/MYSQLSchema
type PolicyDB.sql auto_increment.sql PolicyDB_metadata.sql example_schema.sql> PMSQL.sql
cd C:\Program Files\MySQL\MySQL Server 5.7\bin
@echo on
mysql -u root -p < C:\PM\doc\MYSQLSchema\PMSQL.sql
echo Exit from MYSQL...
pause
