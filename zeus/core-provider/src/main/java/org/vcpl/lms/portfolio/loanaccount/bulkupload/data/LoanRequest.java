package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LoanRequest {
    private int productId;
    private String loanPurposeId;
    private String submittedOnDate;
    private String expectedDisbursementDate;
    private String externalId;
    private String linkAccountId;
    private String createStandingInstructionAtDisbursement;
    private BigDecimal principal;
    private int loanTermFrequency;
    private int loanTermFrequencyType;
    private int numberOfRepayments;
    private int repaymentEvery;
    private int repaymentFrequencyType;
    private int repaymentFrequencyNthDayType;
    private String repaymentFrequencyDayOfWeekType;
    private String repaymentsStartingFromDate;
    private String interestChargedFromDate;
    private double interestRatePerPeriod;
    private int interestType;
    private boolean isEqualAmortization;
    private int amortizationType;
    private int interestCalculationPeriodType;
    private boolean allowPartialPeriodInterestCalcualtion;
    private int transactionProcessingStrategyId;
    private String loanIdToClose;
    private String isTopup;
    private List<LoanCharge> charges;
    private List<LoanCharge> overdueCharges;
    private List<LoanCharge> foreclosureCharges;
    private String dateFormat;
    private String locale;
    private int clientId;
    private String loanType;
    private List<LoanCharge> bounceCharge;
}
