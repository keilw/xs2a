package de.adorsys.aspsp.xs2a.integtest;

import de.adorsys.aspsp.xs2a.integtest.utils.*;
import de.adorsys.psd2.aspsp.profile.domain.*;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.web.client.*;

import javax.annotation.*;


@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "de.adorsys.aspsp.xs2a.integtest.stepdefinitions",
    format = {"json:cucumber-report/cucumber.json"},
    tags = {"~@ignore", "~@TestTag"})
public class CucumberEmbeddedIT {
    @Value("${aspspProfile.baseUrl}")
    private String profileBaseurl;

    @Autowired
    @Qualifier("aspsp-profile")
    private RestTemplate restTemplate;

    @PostConstruct
    public void initEmbeddedProfile(){
        this.restTemplate.put(profileBaseurl+"/aspsp-profile/for-debug/sca-approach",HttpEntityUtils.getHttpEntity("EMBEDDED"));
        initProfile(true);
    }

    private void initProfile(Boolean signingBasketSupported)  {
        AspspSettings settings = restTemplate.getForObject(
            profileBaseurl+"/aspsp-profile", AspspSettings.class);
        settings = new AspspSettings (
            settings.getFrequencyPerDay(),
            settings.isCombinedServiceIndicator(),
            settings.getAvailablePaymentProducts(),
            settings.getAvailablePaymentTypes(),
            settings.isTppSignatureRequired(),
            settings.getPisRedirectUrlToAspsp(),
            settings.getAisRedirectUrlToAspsp(),
            settings.getMulticurrencyAccountLevel(),
            settings.isBankOfferedConsentSupport(),
            settings.getAvailableBookingStatuses(),
            settings.getSupportedAccountReferenceFields(),
            settings.getConsentLifetime(),
            settings.getTransactionLifetime(),
            settings.isAllPsd2Support(),
            settings.isTransactionsWithoutBalancesSupported(),
            signingBasketSupported,
            settings.isPaymentCancellationAuthorizationMandated(),
            settings.isPiisConsentSupported(),
            settings.isDeltaReportSupported(),
            settings.getRedirectUrlExpirationTimeMs()

        );

        this.restTemplate.put(profileBaseurl+"/aspsp-profile/for-debug/aspsp-settings", HttpEntityUtils.getHttpEntity(settings));

    }


}

