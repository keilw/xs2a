# Release notes v. 1.17

## Bugfix: not able to retrieve the payment by redirect id when psu-id is not set in the initial request
When retrieving the payment by redirect id (endpoint GET /psu-api/v1/pis/consent/redirects/{redirect-id} in consent-management) now there is no need
to provide psu data and the payment can be retrieved without it, only by redirect id and instance id.

This also applies for getting payment for cancellation (endpoint GET /psu-api/v1/payment/cancellation/redirect/{redirect-id} in consent-management)

## Bugfix: remove discrepancies between not null constraints in migration files and constraints in Java classes
There were some discrepancies between not null constraints specified in liquibase migration files and constraints specified in Java classes that were removed.
Now tables created via generated DDL should have the same constraints as the tables created via liquibase.

Attention: if you've bypassed the CMS and inserted some records into `pis_payment_data` table directly, with `common_payment_id` 
column set to `null`, you'll have to assign proper values to this column before updating the database.
