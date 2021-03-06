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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.*;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.web.mapper.ConsentModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_UNKNOWN_403;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentControllerTest {
    private static final String CORRECT_PAYMENT_ID = "33333-444444-55555-55555";
    private static final String WRONG_PAYMENT_ID = "wrong_payment_id";
    private static final String REDIRECT_LINK = "http://localhost:4200/consent/confirmation/pis";
    private static final UUID REQUEST_ID = UUID.fromString("ddd36e05-d67a-4830-93ad-9462f71ae1e6");
    private static final String AUTHORISATION_ID = "3e96e9e0-9974-42aa-beb8-003e91416652";
    private static final String CANCELLATION_AUTHORISATION_ID = "d7ba791c-2231-4ed5-8232-cb1ad4cf7332";
    private static final String PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private PaymentModelMapperPsd2 paymentModelMapperPsd2;
    @Mock
    private PaymentModelMapperXs2a paymentModelMapperXs2a;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private AccountReferenceValidationService referenceValidationService;
    @Mock
    private ConsentService consentService;
    @Mock
    private ConsentModelMapper consentModelMapper;
    @Mock
    private PaymentAuthorisationService paymentAuthorisationService;
    @Mock
    private PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private PaymentService xs2aPaymentService;

    @Before
    public void setUp() {
        when(xs2aPaymentService.getPaymentById(eq(SINGLE), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.builder().body(getXs2aPayment()).build());
        when(xs2aPaymentService.getPaymentById(eq(SINGLE), eq(WRONG_PAYMENT_ID)))
            .thenReturn(ResponseObject.builder().fail(new MessageError(ErrorType.PIS_403,
                                                                       new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
        when(aspspProfileService.getPisRedirectUrlToAspsp())
            .thenReturn(REDIRECT_LINK);
        when(referenceValidationService.validateAccountReferences(any()))
            .thenReturn(ResponseObject.builder().build());
    }

    @Before
    public void setUpPaymentServiceMock() {
        when(xs2aPaymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(CORRECT_PAYMENT_ID)))
            .thenReturn(ResponseObject.<TransactionStatus>builder().body(TransactionStatus.ACCP).build());
        when(xs2aPaymentService.getPaymentStatusById(eq(PaymentType.SINGLE), eq(WRONG_PAYMENT_ID)))
            .thenReturn(ResponseObject.<TransactionStatus>builder().fail(new MessageError(ErrorType.PIS_403,
                                                                                          new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403))).build());
    }

    @Test
    public void getPaymentById() {
        doReturn(new ResponseEntity<>(getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP), OK))
            .when(responseMapper).ok(any());

        //Given:
        Object expectedBody = getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus.ACCP);

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                          REQUEST_ID, null, null, null, null, null,
                                                                          null, null, null, null, null,
                                                                          null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualToComparingFieldByField(expectedBody);
    }

    @Test
    public void getPaymentById_Failure() {
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_400, FORMAT_ERROR))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        //When
        ResponseEntity response = paymentController.getPaymentInformation(SINGLE.getValue(), WRONG_PAYMENT_ID,
                                                                          null, null, null, null, null, null,
                                                                          null, null, null, null, null,
                                                                          null, null, null, null);

        //Then
        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private PaymentInitiationTarget2WithStatusResponse getPaymentInitiationResponse(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationTarget2WithStatusResponse response = new PaymentInitiationTarget2WithStatusResponse();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private Object getXs2aPayment() {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(CORRECT_PAYMENT_ID);
        return payment;
    }

    @Test
    public void getTransactionStatusById_Success() {
        doReturn(new ResponseEntity<>(getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP), HttpStatus.OK))
            .when(responseMapper).ok(any(), any());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, CORRECT_PAYMENT_ID)).thenReturn(ResponseObject.<TransactionStatus>builder().body(TransactionStatus.ACCP).build());

        //Given:
        PaymentInitiationStatusResponse200Json expectedBody = getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
        HttpStatus expectedHttpStatus = OK;

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        //Then:
        HttpStatus actualHttpStatus = actualResponse.getStatusCode();
        assertThat(actualHttpStatus).isEqualTo(expectedHttpStatus);
        assertThat(actualResponse.getBody()).isEqualTo(expectedBody);
    }

    private PaymentInitiationStatusResponse200Json getPaymentInitiationStatus(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationStatusResponse200Json response = new PaymentInitiationStatusResponse200Json();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    @Test
    public void getTransactionStatusById_WrongId() {
        doReturn(new ResponseEntity<>(new MessageError(ErrorType.PIS_403,
                                                       new TppMessageInformation(ERROR, RESOURCE_UNKNOWN_403)), FORBIDDEN)).when(responseMapper).ok(any(), any());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());
        when(xs2aPaymentService.getPaymentStatusById(SINGLE, WRONG_PAYMENT_ID)).thenReturn(ResponseObject.<TransactionStatus>builder().fail(createMessageError(ErrorType.PIS_403, RESOURCE_UNKNOWN_403)).build());
        //Given:
        HttpStatus expectedHttpStatus = FORBIDDEN;

        //When:
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse =
            (ResponseEntity<PaymentInitiationStatusResponse200Json>) paymentController.getPaymentInitiationStatus(
                PaymentType.SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null);

        //Then:
        assertThat(actualResponse.getStatusCode()).isEqualTo(expectedHttpStatus);
    }

    @Test
    public void cancelPayment_WithoutAuthorisation_Success() {
        when(responseMapper.ok(any()))
            .thenReturn(new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC), HttpStatus.OK));
        when(xs2aPaymentService.cancelPayment(any(), any(), any())).thenReturn(getCancelPaymentResponseObject(false));

        // Given
        PaymentInitiationCancelResponse204202 response = getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.CANC);
        ResponseEntity<PaymentInitiationCancelResponse204202> expectedResult = new ResponseEntity<>(response, HttpStatus.OK);

        when(xs2aPaymentService.cancelPayment(SINGLE, PRODUCT, CORRECT_PAYMENT_ID)).thenReturn(getCancelPaymentResponseObject(false));
        when(paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(any())).thenReturn(response);
        when(responseMapper.ok(any())).thenReturn(expectedResult);

        // When
        ResponseEntity<PaymentInitiationCancelResponse204202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse204202>) paymentController.cancelPayment(SINGLE.getValue(), PRODUCT,
                                                                                                                                                                     CORRECT_PAYMENT_ID, null, null,
                                                                                                                                                                     null, null, null, null, null,
                                                                                                                                                                     null, null,
                                                                                                                                                                     null, null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actualResult.getBody()).isEqualTo(response);
    }

    @Test
    public void cancelPayment_WithAuthorisation_Success() {
        when(responseMapper.accepted(any()))
            .thenReturn(new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED));
        when(xs2aPaymentService.cancelPayment(any(), any(), any())).thenReturn(getCancelPaymentResponseObject(true));
        when(xs2aPaymentService.cancelPayment(any(), any(), any())).thenReturn(getCancelPaymentResponseObject(true));

        // Given
        PaymentType paymentType = PaymentType.SINGLE;
        ResponseEntity<PaymentInitiationCancelResponse204202> expectedResult = new ResponseEntity<>(getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus.ACTC), HttpStatus.ACCEPTED);

        // When
        ResponseEntity<PaymentInitiationCancelResponse204202> actualResult = (ResponseEntity<PaymentInitiationCancelResponse204202>) paymentController.cancelPayment(paymentType.getValue(),
                                                                                                                                                                     CORRECT_PAYMENT_ID, null, null, null,
                                                                                                                                                                     null, null, null, null, null,
                                                                                                                                                                     null, null,
                                                                                                                                                                     null, null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
        assertThat(actualResult.getBody()).isEqualTo(expectedResult.getBody());
    }

    @Test
    public void cancelPayment_WithoutAuthorisation_Fail_FinalisedStatus() {
        when(xs2aPaymentService.cancelPayment(any(), any(), any())).thenReturn(getErrorOnPaymentCancellation());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_400, FORMAT_ERROR))).thenReturn(ResponseEntity.status(BAD_REQUEST).build());

        // Given
        PaymentType paymentType = PaymentType.SINGLE;
        ResponseEntity<PaymentInitiationCancelResponse204202> expectedResult = ResponseEntity.badRequest().build();

        ResponseEntity actualResult = paymentController.cancelPayment(paymentType.getValue(), PRODUCT,
                                                                      CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                      null, null, null, null, null,
                                                                      null, null,
                                                                      null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    public void cancelPayment_WithAuthorisation_Fail_FinalisedStatus() {
        when(xs2aPaymentService.cancelPayment(any(), any(), any())).thenReturn(getErrorOnPaymentCancellation());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_400, FORMAT_ERROR))).thenReturn(ResponseEntity.status(BAD_REQUEST).build());

        // Given
        PaymentType paymentType = PaymentType.SINGLE;
        ResponseEntity<PaymentInitiationCancelResponse204202> expectedResult = ResponseEntity.badRequest().build();
        // When
        ResponseEntity actualResult = paymentController.cancelPayment(paymentType.getValue(), PRODUCT,
                                                                      CORRECT_PAYMENT_ID, REQUEST_ID, null, null,
                                                                      null, null, null, null,
                                                                      null, null,
                                                                      null, null, null, null, null);

        // Then:
        assertThat(actualResult.getStatusCode()).isEqualTo(expectedResult.getStatusCode());
    }

    @Test
    public void getPaymentInitiationScaStatus_success() {
        ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> responseObject = ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                                                                                     .body(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED)
                                                                                     .build();
        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(any(String.class), any(String.class)))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse(ScaStatus.RECEIVED)))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
        ScaStatusResponse expected = buildScaStatusResponse(ScaStatus.RECEIVED);

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationScaStatus(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                                AUTHORISATION_ID, REQUEST_ID,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    public void getPaymentInitiationScaStatus_failure() {
        when(paymentAuthorisationService.getPaymentInitiationAuthorisationScaStatus(WRONG_PAYMENT_ID, AUTHORISATION_ID))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity actual = paymentController.getPaymentInitiationScaStatus(SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID,
                                                                                AUTHORISATION_ID, REQUEST_ID,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null,
                                                                                null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    public void getPaymentCancellationScaStatus_success() {
        ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> responseObject = ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                                                                                     .body(de.adorsys.psd2.xs2a.core.sca.ScaStatus.RECEIVED)
                                                                                     .build();
        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(any(String.class), any(String.class)))
            .thenReturn(responseObject);
        doReturn(ResponseEntity.ok(buildScaStatusResponse(ScaStatus.RECEIVED)))
            .when(responseMapper).ok(eq(responseObject), any());

        // Given
        ScaStatusResponse expected = buildScaStatusResponse(ScaStatus.RECEIVED);

        // When
        ResponseEntity actual = paymentController.getPaymentCancellationScaStatus(SINGLE.getValue(), PRODUCT, CORRECT_PAYMENT_ID,
                                                                                  CANCELLATION_AUTHORISATION_ID, REQUEST_ID,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(OK);
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    public void getPaymentCancellationScaStatus_failure() {
        when(paymentCancellationAuthorisationService.getPaymentCancellationAuthorisationScaStatus(WRONG_PAYMENT_ID, CANCELLATION_AUTHORISATION_ID))
            .thenReturn(buildScaStatusError());
        when(responseErrorMapper.generateErrorResponse(createMessageError(ErrorType.PIS_403, RESOURCE_UNKNOWN_403))).thenReturn(ResponseEntity.status(FORBIDDEN).build());

        // When
        ResponseEntity actual = paymentController.getPaymentCancellationScaStatus(SINGLE.getValue(), PRODUCT, WRONG_PAYMENT_ID,
                                                                                  CANCELLATION_AUTHORISATION_ID, REQUEST_ID,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null,
                                                                                  null, null, null);

        // Then
        assertThat(actual.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    private ResponseObject<CancelPaymentResponse> getCancelPaymentResponseObject(boolean startAuthorisationRequired) {
        CancelPaymentResponse response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(startAuthorisationRequired);
        return ResponseObject.<CancelPaymentResponse>builder().body(response).build();
    }

    private PaymentInitiationCancelResponse204202 getPaymentInitiationCancelResponse200202(de.adorsys.psd2.model.TransactionStatus transactionStatus) {
        PaymentInitiationCancelResponse204202 response = new PaymentInitiationCancelResponse204202();
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private ResponseObject<CancelPaymentResponse> getErrorOnPaymentCancellation() {
        return ResponseObject.<CancelPaymentResponse>builder()
                   .fail(new MessageError(ErrorType.PIS_400, new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR)))
                   .build();
    }

    private ScaStatusResponse buildScaStatusResponse(ScaStatus scaStatus) {
        return new ScaStatusResponse().scaStatus(scaStatus);
    }

    private ResponseObject<de.adorsys.psd2.xs2a.core.sca.ScaStatus> buildScaStatusError() {
        return ResponseObject.<de.adorsys.psd2.xs2a.core.sca.ScaStatus>builder()
                   .fail(new MessageError(ErrorType.PIS_403, new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_403)))
                   .build();
    }

    private MessageError createMessageError(ErrorType errorType, MessageErrorCode errorCode) {
        return new MessageError(errorType, new TppMessageInformation(MessageCategory.ERROR, errorCode));
    }

}
