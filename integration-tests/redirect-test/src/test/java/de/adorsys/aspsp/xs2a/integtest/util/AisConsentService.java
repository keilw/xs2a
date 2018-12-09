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

package de.adorsys.aspsp.xs2a.integtest.util;

import com.fasterxml.jackson.databind.*;
import de.adorsys.aspsp.xs2a.integtest.config.rest.consent.*;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.consent.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;
import org.springframework.web.client.*;

import javax.validation.constraints.*;
import java.io.*;
import java.time.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.IOUtils.*;

@Slf4j
@Service
public class AisConsentService {

    @Qualifier("consent")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public AisConsentService(RestTemplate consentRestTemplate, AisConsentRemoteUrls remoteAisConsentUrls) {
        this.consentRestTemplate = consentRestTemplate;
        this.remoteAisConsentUrls = remoteAisConsentUrls;
    }

    /**
     * Sends a post request to the consent management for creating a consent
     *
     * @return Consent Id
     */
    public String createConsentWithStatus(String consentStatus, String filename) throws IOException {
        HttpEntity entity = getConsentFromJsonData(filename);
        log.info("////entity request consent////  " + entity.toString());
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), entity, CreateAisConsentResponse.class).getBody();

        Optional<ConsentStatus> consentStatusOptional = ConsentStatus.fromValue(consentStatus);
        changeAccountConsentStatus(createAisConsentResponse.getConsentId(), consentStatusOptional.get());

        return createAisConsentResponse.getConsentId();
    }

    /**
     * @param consentId     as a String
     * @param consentStatus which represents the new Consent Status
     */
    public void changeAccountConsentStatus(@NotNull String consentId, ConsentStatus consentStatus) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, consentStatus);
    }

    private HttpEntity getConsentFromJsonData(String dataFileName) throws IOException {
        CreateAisConsentRequest aisConsentRequest = mapper.readValue(resourceToString("/data-input/ais/" + dataFileName, UTF_8), CreateAisConsentRequest.class);
        aisConsentRequest.setValidUntil(LocalDate.now().plusDays(90));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return new HttpEntity<>(aisConsentRequest, headers);
    }


}

