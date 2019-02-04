# Release notes v. 1.17

## Bugfix: not able to retrieve the payment by redirect id when psu-id is not set in the initial request
When retrieving the payment by redirect id (endpoint GET /psu-api/v1/pis/consent/redirects/{redirect-id} in consent-management) now there is no need
to provide psu data and the payment can be retrieved without it, only by redirect id and instance id.

This also applies for getting payment for cancellation (endpoint GET /psu-api/v1/payment/cancellation/redirect/{redirect-id} in consent-management)

## Bugfix: Remove PSU Data from endpoint for getting consent by redirect id in CMS-PSU-API
PSU Data is no longer required for getting consent by redirect id.
As a result, headers `psu-id`, `psu-id-type`, `psu-corporate-id` and `psu-corporate-id-type` are no longer used in `psu-api/v1/ais/consent/redirect/{redirect-id}` endpoint.
PsuIdData was also removed as an argument from `de.adorsys.psd2.consent.psu.api.CmsPsuAisService#checkRedirectAndGetConsent` method.

## Implement interface for exporting PIIS consents from CMS to ASPSP
Provided implementation for Java interface `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService` 
that allows exporting PIIS consents by ASPSP account id, TPP ID and PSU ID Data.

From now on these endpoints are fully functional:

| Method | Endpoint                                         | Description                                                                                               |
|--------|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| GET    | /aspsp-api/v1/piis/consents/account/{account-id} | Returns a list of consents by given mandatory aspsp account id, optional creation date and instance ID    |
| GET    | /aspsp-api/v1/piis/consents/psu                  | Returns a list of consents by given mandatory PSU ID Data, optional creation date and instance ID         |
| GET    | /aspsp-api/v1/piis/consents/tpp/{tpp-id}         | Returns a list of consents by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID |
