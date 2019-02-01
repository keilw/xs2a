# Release notes v. 1.17

## Bugfix: remove not null constraint from payment in PisCommonPaymentData
Removed not null constraint from `payment` column in `pis_common_payment` that could be present if the table was created via generated DDL.
