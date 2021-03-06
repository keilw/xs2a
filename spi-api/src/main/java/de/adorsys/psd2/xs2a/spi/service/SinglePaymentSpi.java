/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Interface to be used for single payment SPI implementation
 */
public interface SinglePaymentSpi extends PaymentSpi<SpiSinglePayment, SpiSinglePaymentInitiationResponse> {
    @Override
    @NotNull
    SpiResponse<SpiSinglePaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData initialAspspConsentData);

    @Override
    @NotNull
    SpiResponse<SpiSinglePayment> getPaymentById(@NotNull SpiContextData contextData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData aspspConsentData);

    @Override
    @NotNull
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull SpiSinglePayment payment, @NotNull AspspConsentData aspspConsentData);
}
