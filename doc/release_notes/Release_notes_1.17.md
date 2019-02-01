# Release notes v. 1.17

## Bugfix: remove discrepancies between not null constraints in migration files and constraints in Java classes
There were some discrepancies between not null constraints specified in liquibase migration files and constraints specified in Java classes that were removed.
Now tables created via generated DDL should have the same constraints as the tables created via liquibase.

Attention: if you've bypassed the CMS and inserted some records into `pis_payment_data` table with `common_payment_id` 
column set to `null` directly, you'll have to assign some value to this column before updating the database.
