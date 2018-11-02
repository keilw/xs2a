/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.onlinebanking.web;

import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/ais/psu")
@Api(value = "api/v1/ais/psu", tags = "PSU AIS, Consents", description = "Provides access to consent management system for PSU AIS")
public class CmsPsuAisConsentController {
    private final CmsPsuAisService cmsPsuAisService;

    @PutMapping(path = "/consent/{consent-id}/update-psu-data")
    @ApiOperation(value = "Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation).")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updatePsuDataInConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.updatePsuDataInConsent(psuIdData, consentId), HttpStatus.OK);
    }

    @PutMapping(path = "consent/{consent-id}")
    @ApiOperation(value = "Returns AIS Consent object by its ID.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AisAccountConsent> getConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody PsuIdData psuIdData) {
        return cmsPsuAisService.getConsent(psuIdData, consentId)
                   .map(aisAccountConsent -> new ResponseEntity<>(aisAccountConsent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping(path = "consent/{consent-id}/authorizations/{authorization-id}/status/{status}")
    @ApiOperation(value = "Updates a Status of AIS Consent Authorisation by its ID and PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updateAuthorisationStatus(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised' 'failed' 'exempted'.", example = "RECEIVED")
        @PathVariable("status") String status,
        @ApiParam(name = "authorization-id", value = "The consent authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId,
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.updateAuthorisationStatus(psuIdData, consentId, authorizationId, ScaStatus.valueOf(status)), HttpStatus.OK);
    }

    @PutMapping(path = "consent/{consent-id}/confirm-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to VALID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> confirmConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.confirmConsent(psuIdData, consentId), HttpStatus.OK);
    }

    @PutMapping(path = "consent/{consent-id}/reject-consent")
    @ApiOperation(value = "Puts a Status of AIS Consent object by its ID and PSU ID to REJECTED")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> rejectConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.rejectConsent(psuIdData, consentId), HttpStatus.OK);
    }

    @PutMapping(path = "consents")
    @ApiOperation(value = "Returns a list of AIS Consent objects by PSU ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<AisAccountConsent>> getConsentsForPsu(
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.getConsentsForPsu(psuIdData), HttpStatus.OK);
    }

    @PutMapping(path = "consent/{consent-id}/revoke-consent")
    @ApiOperation(value = "Revokes AIS Consent object by its ID. Consent gets status Revoked by PSU.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Boolean.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> revokeConsent(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody PsuIdData psuIdData) {
        return new ResponseEntity<>(cmsPsuAisService.revokeConsent(psuIdData, consentId), HttpStatus.OK);
    }
}
