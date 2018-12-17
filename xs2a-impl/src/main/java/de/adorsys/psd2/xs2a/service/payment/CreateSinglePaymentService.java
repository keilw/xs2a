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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisCommonPayment;
import de.adorsys.psd2.xs2a.domain.consent.Xsa2CreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.PisCommonPaymentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateSinglePaymentService implements CreatePaymentService<SinglePayment, SinglePaymentInitiationResponse> {
    private final ScaPaymentService scaPaymentService;
    private final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final PisScaAuthorisationService pisScaAuthorisationService;
    private final AuthorisationMethodService authorisationMethodService;
    private final PisCommonPaymentDataService pisCommonPaymentDataService;

    /**
     * Initiates single payment
     *
     * @param singlePayment               Single payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param commonPayment               common payment information
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created periodic payment or corresponding error
     */
    @Override
    public ResponseObject<SinglePaymentInitiationResponse> createPayment(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo, Xs2aPisCommonPayment commonPayment) {
        String externalPaymentId = commonPayment.getPaymentId();

        // we need to get decrypted payment ID
        String internalPaymentId = pisCommonPaymentDataService.getInternalPaymentIdByEncryptedString(externalPaymentId);
        singlePayment.setPaymentId(internalPaymentId);

        SinglePaymentInitiationResponse response = scaPaymentService.createSinglePayment(singlePayment, tppInfo, paymentInitiationParameters.getPaymentProduct(), commonPayment);
        response.setPisConsentId(commonPayment.getPaymentId());

        singlePayment.setTransactionStatus(response.getTransactionStatus());

        pisCommonPaymentService.updateSinglePaymentInPisConsent(singlePayment, paymentInitiationParameters, commonPayment.getPaymentId());

        boolean implicitMethod = authorisationMethodService.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred());
        if (implicitMethod) {
            Optional<Xsa2CreatePisAuthorisationResponse> consentAuthorisation = pisScaAuthorisationService.createCommonPaymentAuthorisation(externalPaymentId, PaymentType.SINGLE, paymentInitiationParameters.getPsuData());
            if (!consentAuthorisation.isPresent()) {
                return ResponseObject.<SinglePaymentInitiationResponse>builder()
                           .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                           .build();
            }
            Xsa2CreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorizationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
        }

        // we need to return encrypted payment ID
        response.setPaymentId(externalPaymentId);

        return ResponseObject.<SinglePaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }
}
