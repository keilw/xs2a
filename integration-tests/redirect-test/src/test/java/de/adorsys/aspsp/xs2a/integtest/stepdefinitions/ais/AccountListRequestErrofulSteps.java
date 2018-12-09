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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.ais;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import cucumber.api.java.en.*;
import de.adorsys.aspsp.xs2a.integtest.model.*;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.*;
import de.adorsys.aspsp.xs2a.integtest.util.*;
import de.adorsys.aspsp.xs2a.integtest.utils.*;
import de.adorsys.psd2.model.*;
import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.IOUtils.*;

@Slf4j
@FeatureFileSteps
public class AccountListRequestErrofulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;


    //@Given("^PSU already has an existing (.*) consent (.*)$")
    //in commonStep

    @And("^PSU wants to get a list of accounts errorful using (.*)$")
    public void psu_wants_to_get_a_list_of_accounts_errorful_using(String dataFileName) throws IOException {
        TestData<HashMap, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/ais/account/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, TppMessages>>() {
            });

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }

    @When("^PSU sends get request$")
    public void psu_sends_get_request() throws HttpClientErrorException, IOException {

        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
            context.getAccessToken());
        log.info("////entity request list account////  " + entity.toString());
        try {
            ResponseEntity<AccountList> response = restTemplate.exchange(
                context.getBaseUrl() + "/accounts",
                HttpMethod.GET,
                entity,
                AccountList.class);
        } catch (RestClientResponseException rex) {
            context.handleRequestError(rex);
        }
    }

    //@Then("^an error response code is displayed and an appropriate error response is shown$")
    //See commonStep

}

