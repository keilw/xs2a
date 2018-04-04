package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;

public interface PaymentSpi {

    SpiTransactionStatus getPaymentStatusById(String paymentId);

    String createPaymentInitiation(SpiSinglePayments paymentInitiationRequest,
                                   boolean tppRedirectPreferred);

}
