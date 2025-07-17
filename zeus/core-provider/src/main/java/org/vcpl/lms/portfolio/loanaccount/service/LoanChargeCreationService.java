package org.vcpl.lms.portfolio.loanaccount.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.charge.domain.ChargeRepository;
import org.vcpl.lms.portfolio.charge.exception.ChargeNotFoundException;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeTransactionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class LoanChargeCreationService implements LoanChargeActions.Creation {
    private static final Logger LOG = LoggerFactory.getLogger(LoanChargeCreationService.class);

    private final LoanRepository loanRepository;
    private final ChargeRepository chargeRepository;
    private final PlatformSecurityContext context;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final LoanChargePaymentService loanChargePaymentService;

    @Override
    public LoanCharge create(ChargeTransactionRequest chargeTransactionRequest) {
        Loan loan = Objects.nonNull(chargeTransactionRequest.getLoanId())
                ? loanRepository.getReferenceById(chargeTransactionRequest.getLoanId())
                : loanRepository.getByAccountNumber(chargeTransactionRequest.getLoanAccountNo())
                    .orElseThrow(() -> {
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                        "Loan Account Number does not exist - " + chargeTransactionRequest.getLoanAccountNo(),
                        null, null);
                        return new PlatformApiDataValidationException(List.of(error));
                    });

        Charge charge = chargeRepository.getReferenceById(chargeTransactionRequest.getChargeId());
        LoanRepaymentScheduleInstallment installment = loan.fetchInstallmentByDate(chargeTransactionRequest.getTransactionDate());
        LoanProductFeesCharges productFeesCharge = loan.getLoanProduct().getLoanProductFeesCharges().stream()
                .filter(loanProductFeesCharge -> loanProductFeesCharge.getCharge().getId().equals(charge.getId()))
                .findAny()
                .orElseThrow(()-> new ChargeNotFoundException(charge.getId()));
        LoanCharge loanCharge = LoanChargeActions.createCharge(loan,context.authenticatedUser(),installment,charge,
                chargeTransactionRequest.getTransactionDate(),productFeesCharge.getSelfShare(),productFeesCharge.getPartnerShare(),chargeTransactionRequest.getAmount());
        LOG.info("Creating Loan Charge  LoanId: {} ChargeId: {} ChargeTimeType: {}", loanCharge.getLoan().getId(), charge.getId() ,loanCharge.getChargeTime());
        loan.getCharges().add(loanCharge);
        installment.setFeeChargesCharged(installment.getFeeChargesCharged().add(loanCharge.getAmount()));
        installment.setSelfFeeChargesCharged(Objects.requireNonNullElse(installment.getSelfFeeChargesCharged(), BigDecimal.ZERO)
                        .add(loanCharge.getSelfAmountOutstanding()));
        installment.setPartnerFeeChargesCharged(Objects.requireNonNullElse(installment.getPartnerFeeChargesCharged(), BigDecimal.ZERO)
                        .add(loanCharge.getPartnerAmountOutstanding()));
        loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        if (charge.isAdhocCharge()) {
            LOG.info("Processing Adhoc Charge Payment Loan Id - {}",chargeTransactionRequest.getLoanId());
            Predicate<LoanCharge> loanChargePredicate = loanChargePaymentService.deriveLoanChargePredicate(chargeTransactionRequest);
            final BigDecimal[] receivedPayment = {loanCharge.getAmount()};

            LoanTransaction loanTransaction = new LoanTransaction();
            loanTransaction.setDateOf(DateUtils.convertLocalDateToDate(chargeTransactionRequest.getTransactionDate()));

            LoanTransaction chargePaymentTransaction = loanChargePaymentService.payCharges(loan, loanChargePredicate, receivedPayment, loanTransaction);
            loan.getLoanTransactions().add(chargePaymentTransaction);
            loanChargePaymentService.summarizeRepaymentScheduleForChargesRepaid(loan, chargePaymentTransaction);
            loan.getSummary().updateSummary(loan.getCurrency(), loan.getPrincpal(), loan.getRepaymentScheduleInstallments(), loanSummaryWrapper, true,
                    loan.charges(), loan.getSelfPrincipaAmount(), loan.getPartnerPrincipalAmount());
        }
        loanRepository.save(loan);
        return loanCharge;
    }
}
