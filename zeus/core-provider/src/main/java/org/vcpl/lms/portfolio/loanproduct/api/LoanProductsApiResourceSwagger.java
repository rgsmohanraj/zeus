/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.loanproduct.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.vcpl.lms.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.vcpl.lms.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.core.data.EnumOptionData;
import org.vcpl.lms.organisation.monetary.data.CurrencyData;
import org.vcpl.lms.portfolio.charge.data.ChargeData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductBorrowerCycleVariationData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductGuaranteeData;
import org.vcpl.lms.portfolio.loanproduct.data.LoanProductInterestRecalculationData;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesChargesData;
import org.vcpl.lms.portfolio.partner.data.PartnerData;
import org.vcpl.lms.portfolio.rate.data.RateData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


final class LoanProductsApiResourceSwagger {

    private LoanProductsApiResourceSwagger() {}

    @Schema(description = "PostLoanProductsRequest")
    public static final class PostLoanProductsRequest {

        private PostLoanProductsRequest() {}

        @Schema(example = "NOCPL PRODUCT")
        public String name;

        @Schema(example = "LPAA")
        public String shortName;

        @Schema(example = "CONOCPL")
        public String loanAccNoPreference;

        @Schema(example = "Loan Product Creation")
        public  String description;

        @Schema(example = "Actual")
        public  String brokenInterestCalculationPeriod;

        @Schema(example = "Strategy1")
        public  String repaymentStrategyForNpaId;

        @Schema(example = "1")
        public  String brokenInterestStrategy;

        @Schema(example = "false")
        public  boolean useDaysInMonthForLoanProvisioning;

        @Schema(example = "false")
        public  boolean divideByThirtyForPartialPeriod;

        @Schema(example = "true")
        public  boolean allowApprovalOverAmountApplied;

        @Schema(example = "true")
        public  boolean includeInBorrowerCycle;

        @Schema(example = "true")
        public  boolean useBorrowerCycle;

        @Schema(example = "10 March 2023")
        public  LocalDate startDate;

        @Schema(example = "1")
        public Integer principalThresholdForLastInstallment;

        @Schema(example = "10 March 2024")
        public  LocalDate closeDate;

        @Schema(example = "10000.00")
        public   BigDecimal principal;

        @Schema(example = "1")
        public  BigDecimal minPrincipal;

        @Schema(example = "100000000")
        public  BigDecimal maxPrincipal;

        @Schema(example = "12")
        public  Integer numberOfRepayments;

        @Schema(example = "1")
        public  Integer minNumberOfRepayments;

        @Schema(example = "24")
        public  Integer maxNumberOfRepayments;

        @Schema(example = "1")
        public  Integer repaymentEvery;

        @Schema(example = "2")
        public Integer repaymentFrequencyType;

        @Schema(example = "12")
        public  BigDecimal interestRatePerPeriod;

        @Schema(example = "12")
        public  BigDecimal minInterestRatePerPeriod;

        @Schema(example = "36")
        public  BigDecimal maxInterestRatePerPeriod;

        @Schema(example = "3")
        public  Integer interestRateFrequencyType;

        @Schema(example = "false")
        public  boolean isLinkedToFloatingInterestRates;

        @Schema(example = "false")
        public  boolean allowVariableInstallments;

        @Schema(example = "true")
        public  boolean enableColendingLoan;

        @Schema(example = "true")
        public  boolean byPercentageSplit;

        @Schema(example = "100")
        public  Integer selfPrincipalShare;

        @Schema(example = "100")
        public  Integer selfFeeShare;

        @Schema(example = "")
        public  Integer selfPenaltyShare;

        @Schema(example = "")
        public  Integer selfOverpaidShares;

        @Schema(example = "100")
        public  BigDecimal selfInterestRate;

        @Schema(example = "100")
        public  Integer principalShare;

        @Schema(example = "100")
        public  Integer feeShare;

        @Schema(example = "100")
        public  Integer penaltyShare;

        @Schema(example = "100")
        public  Integer overpaidShare;

        @Schema(example = "100")
        public  BigDecimal interestRate;

        @Schema(example = "0")
        public  Integer partnerPrincipalShare;

        @Schema(example = "0")
        public  Integer partnerFeeShare;

        @Schema(example = "")
        public  Integer partnerPenaltyShare;

        @Schema(example = "")
        public  Integer partnerOverpaidShare;

        @Schema(example = "0")
        public  BigDecimal partnerInterestRate;

        @Schema(example = "1")
        public  Long partnerId;

        @Schema(example = "false")
        public  boolean enableFeesWiseBifacation;

        @Schema(example = "false")
        public  boolean enableChargeWiseBifacation;

        @Schema(example = "true")
        public  boolean enableOverDue;

        @Schema(example = "10")
        public  Integer colendingCharge;

        @Schema(example = "100")
        public  Integer selfCharge;

        @Schema(example = "0")
        public  Integer partnerCharge;

        @Schema(example = "false")
        public  boolean selectAcceptedDates;

        @Schema(example = "false")
        public  boolean applyPrepaidLockingPeriod;

        @Schema(example = "false")
        public  boolean applyForeclosureLockingPeriod;

        @Schema(example = "1")
        public  Integer amortizationType;

        @Schema(example = "0")
        public  Integer interestType;

        @Schema(example = "0")
        public  Integer interestCalculationPeriodType;

        @Schema(example = "")
        public  BigDecimal inArrearsTolerance;

        @Schema(example = "1")
        public  Integer daysInMonthType;

        @Schema(example = "1")
        public  Integer daysInYearType;

        @Schema(example = "false")
        public  boolean isInterestRecalculationEnabled;

        @Schema(example = "45")
        public  Integer minimumDaysBetweenDisbursalAndFirstRepayment;

        @Schema(example = "50")
        public  BigDecimal gstLiabilityByVcpl;

        @Schema(example = "50")
        public  BigDecimal gstLiabilityByPartner;

        @Schema(example = "1")
        public  Integer transactionTypePreference;

        @Schema(example = "100")
        public  BigDecimal vcplShareInBrokenInterest;

        @Schema(example = "0")
        public  BigDecimal partnerShareInBrokenInterest;

        @Schema(example = "12")
        public  Integer monitoringTriggerPar30;

        @Schema(example = "12")
        public  Integer monitoringTriggerPar90;

        @Schema(example = "69")
        public  Integer fldgLogic;

        @Schema(example = "85")
        public  Integer assetClass;

        @Schema(example = "")
        public Collection<ChargeData> charges;

        @Schema(example = "1")
        public  Integer accountingRule;

        @Schema(example = "false")
        public  boolean isEqualAmortization;

        @Schema(example = "100896757425")
        public  Long disbursementAccountNumber;

        @Schema(example = "657465736878")
        public  Long collectionAccountNumber;

        @Schema(example = "true")
        public  Boolean isPennyDropEnabled;

        @Schema(example = "true")
        public  Boolean isBankDisbursementEnabled;

        @Schema(example = "true")
        public  Boolean servicerFeeInterestConfigEnabled;

        @Schema(example = "true")
        public  Boolean servicerFeeChargesConfigEnabled;
        @Schema(example = "true")
        public  boolean enableDedupe;
        @Schema(example = "PAN/AADHAAR")
        public Integer dedupeType;
        @Schema(example = "bank-account-key")
        public Integer disbursementBankAccountName;

        @Schema(example = "INR")
        public String currencyCode;

        @Schema(example = "en_GB")
        public String locale;

        @Schema(example = "2")
        public Integer digitsAfterDecimal;

        @Schema(example = "UP")
        public String emiRoundingMode;

        @Schema(example = "1")
        public Integer emimultiples;

        @Schema(example = "UP")
        public String interestRoundingMode;

        @Schema(example = "1")
        public Integer interestDecimal;

        @Schema(example = "1")
        public Integer interestDecimalRegex;

        @Schema(example = "0")
        public Integer emiDecimalRegex;

        @Schema(example = "Loan Strategy1")
        public String loanForeclosureStrategy;

        @Schema(example = "2")
        public String brokenInterestDaysInYears;

        @Schema(example = "1")
        public Long transactionProcessingStrategyId;

        @Schema(example = "1")
        public Long graceOnPrincipalPayment;

        @Schema(example = "10")
        public Integer graceOnInterestPayment;

        @Schema(example = "1")
        public Integer graceOnInterestCharged;

        @Schema(example = "1")
        public String repaymentStrategyForNpa;

        @Schema(example = "false")
        public boolean canDefineInstallmentAmount;

        @Schema(example = "1")
        public Integer graceOnArrearsAgeing;

        @Schema(example = "1")
        public Integer overdueDaysForNPA;

        @Schema(example = "false")
        public Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;

        @Schema(example = "1002")
        public BigDecimal aumSlabRate;

        @Schema(example = "false")
        public Boolean disallowExpectedDisbursements;

        @Schema(example = "false")
        public Boolean canUseForTopup;

        @Schema(example = "false")
        public Boolean holdGuaranteeFunds;

        @Schema(example = "false")
        public Boolean multiDisburseLoan;

        @Schema(example = "129")
        public Integer insuranceApplicability;

        @Schema(example = "1")
        public Integer disbursement;

        @Schema(example = "1")
        public Integer collection;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

//        @Schema(example = "false")
//        public BigDecimal aumSlabRate;
//
//        @Schema(example = "false")
//        public BigDecimal aumSlabRate;








    }

    @Schema(description = "PostLoanProductsResponse")
    public static final class PostLoanProductsResponse {

        private PostLoanProductsResponse() {}

        @Schema(example = "3")
        public Integer resourceId;
    }

    @Schema(description = "GetLoanProductsResponse")
    public static final class GetLoanProductsResponse {

        private GetLoanProductsResponse() {}

        static final class GetLoanProductsCurrency {

            private GetLoanProductsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetLoanProductsRepaymentFrequencyType {

            private GetLoanProductsRepaymentFrequencyType() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        static final class GetLoanProductsInterestRateFrequencyType {

            private GetLoanProductsInterestRateFrequencyType() {}

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.years")
            public String code;
            @Schema(example = "Per year")
            public String description;
        }

        static final class GetLoanProductsAmortizationType {

            private GetLoanProductsAmortizationType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "amortizationType.equal.installments")
            public String code;
            @Schema(example = "Equal installments")
            public String description;
        }

        static final class GetLoanProductsInterestType {

            private GetLoanProductsInterestType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "interestType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetLoansProductsInterestCalculationPeriodType {

            private GetLoansProductsInterestCalculationPeriodType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "interestCalculationPeriodType.same.as.repayment.period")
            public String code;
            @Schema(example = "Same as repayment period")
            public String description;
        }

        static final class GetLoansProductsDaysInMonthType {

            private GetLoansProductsDaysInMonthType() {}

            @Schema(example = "30")
            public Integer id;
            @Schema(example = "DaysInMonthType.days360")
            public String code;
            @Schema(example = "30 Days")
            public String description;
        }

        static final class GetLoansProductsDaysInYearType {

            private GetLoansProductsDaysInYearType() {}

            @Schema(example = "360")
            public Integer id;
            @Schema(example = "DaysInYearType.days360")
            public String code;
            @Schema(example = "360 Days")
            public String description;
        }

        static final class GetLoanProductsInterestRecalculationData {

            private GetLoanProductsInterestRecalculationData() {}

            static final class GetLoanProductsInterestRecalculationCompoundingType {

                private GetLoanProductsInterestRecalculationCompoundingType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "interestRecalculationCompoundingMethod.fee")
                public String code;
                @Schema(example = "Fee")
                public String description;
            }

            static final class GetLoanProductsInterestRecalculationCompoundingFrequencyType {

                private GetLoanProductsInterestRecalculationCompoundingFrequencyType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "interestRecalculationFrequencyType.same.as.repayment.period")
                public String code;
                @Schema(example = "Same as repayment period")
                public String description;
            }

            static final class GetLoanProductsRescheduleStrategyType {

                private GetLoanProductsRescheduleStrategyType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "loanRescheduleStrategyMethod.reduce.number.of.installments")
                public String code;
                @Schema(example = "Reduce number of installments")
                public String description;
            }

            static final class GetLoanProductsPreClosureInterestCalculationStrategy {

                private GetLoanProductsPreClosureInterestCalculationStrategy() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "loanPreClosureInterestCalculationStrategy.tillPreClosureDate")
                public String code;
                @Schema(example = "Till preclose Date")
                public String description;
            }

            @Schema(example = "3")
            public Integer id;
            @Schema(example = "1")
            public Integer productId;
            public GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType interestRecalculationCompoundingFrequencyType;
            public GetLoanProductsInterestRecalculationData.GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsInterestRecalculationCompoundingFrequencyType recalculationRestFrequencyType;
            public GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
            @Schema(example = "true")
            public Boolean isArrearsBasedOnOriginalSchedule;
        }

        static final class GetLoanProductsAccountingRule {

            private GetLoanProductsAccountingRule() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "accountingRuleType.cash")
            public String code;
            @Schema(example = "CASH BASED")
            public String description;
        }
        static final class GetLoanProductsDeDupeOptions {

            private GetLoanProductsDeDupeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "Dedupe.aadhaar")
            public String code;
            @Schema(example = "AADHAAR")
            public String value;
        }

        static final class GetLoanProductsDisbursementBankAccountNameOptions {

            private GetLoanProductsDisbursementBankAccountNameOptions() {}

            @Schema(example = "10")
            public Integer id;
            @Schema(example = "account-key")
            public String name;
            @Schema(example = "1")
            public Integer position;
        }

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "personal loan product")
        public String name;
        @Schema(example = "pe1")
        public String shortName;
        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        @Schema(example = "[2013, 9, 2]")
        public LocalDate startDate;
        @Schema(example = "[2014, 2, 7]")
        public LocalDate endDate;
        @Schema(example = "loanProduct.active")
        public String status;
        public GetLoanProductsCurrency currency;
        @Schema(example = "10000.000000")
        public Float principal;
        @Schema(example = "5000.000000")
        public Float minPrincipal;
        @Schema(example = "15000.000000")
        public Float maxPrincipal;
        @Schema(example = "10")
        public Integer numberOfRepayments;
        @Schema(example = "5")
        public Integer minNumberOfRepayments;
        @Schema(example = "15")
        public Integer maxNumberOfRepayments;
        @Schema(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "15.000000")
        public Float interestRatePerPeriod;
        public GetLoanProductsResponse.GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "15.000000")
        public Float annualInterestRate;
        public GetLoanProductsAmortizationType amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoanProductsInterestType interestType;
        public GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "1")
        public Integer transactionProcessingStrategyId;
        @Schema(example = "ZEUS LMS")
        public String transactionProcessingStrategyName;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoansProductsDaysInMonthType daysInMonthType;
        public GetLoansProductsDaysInYearType daysInYearType;
        @Schema(example = "true")
        public Boolean isInterestRecalculationEnabled;
        public GetLoanProductsInterestRecalculationData interestRecalculationData;
        public GetLoanProductsResponse.GetLoanProductsAccountingRule accountingRule;
        @Schema(example = "0")
        public Integer principalThresholdForLastInstalment;
        @Schema(example = "true")
        public Boolean enableDedupe;
        @Schema(example = "1")
        public Integer dedupeType;
        @Schema(example = "bank-account-key")
        public Integer disbursementBankAccountName;
        public Set<GetLoanProductsDisbursementBankAccountNameOptions> disbursementBankAccountNameOptions;
        public Set<GetLoanProductsDeDupeOptions> dedupeOptions;
    }

    @Schema(description = "GetLoanProductsTemplateResponse")
    public static final class GetLoanProductsTemplateResponse {

        private GetLoanProductsTemplateResponse() {}

        static final class GetLoanProductsTemplateCurrency {

            private GetLoanProductsTemplateCurrency() {}

            @Schema(example = "")
            public String code;
            @Schema(example = "")
            public String name;
            @Schema(example = "0")
            public Integer decimalPlaces;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "")
            public String displaySymbol;
            @Schema(example = "")
            public String nameCode;
            @Schema(example = "[]")
            public String displayLabel;
        }

        static final class GetLoanProductsRepaymentTemplateFrequencyType {

            private GetLoanProductsRepaymentTemplateFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "repaymentFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Months")
            public String description;
        }

        static final class GetLoanProductsInterestRateTemplateFrequencyType {

            private GetLoanProductsInterestRateTemplateFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Per month")
            public String description;
        }

        static final class GetLoanProductsInterestTemplateType {

            private GetLoanProductsInterestTemplateType() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "interestType.declining.balance")
            public String code;
            @Schema(example = "Declining Balance")
            public String description;
        }

        static final class GetLoanProductsAccountingRule {

            private GetLoanProductsAccountingRule() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "accountingRuleType.none")
            public String code;
            @Schema(example = "NONE")
            public String description;
        }

        static final class GetLoansProductsDaysInMonthTemplateType {

            private GetLoansProductsDaysInMonthTemplateType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "DaysInMonthType.actual")
            public String code;
            @Schema(example = "Actual")
            public String description;
        }

        static final class GetLoanProductsDaysInYearTemplateType {

            private GetLoanProductsDaysInYearTemplateType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "DaysInYearType.actual")
            public String code;
            @Schema(example = "Actual")
            public String description;
        }

        static final class GetLoanProductsInterestRecalculationTemplateData {

            private GetLoanProductsInterestRecalculationTemplateData() {}

            static final class GetLoanProductsInterestRecalculationCompoundingType {

                private GetLoanProductsInterestRecalculationCompoundingType() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "interestRecalculationCompoundingMethod.none")
                public String code;
                @Schema(example = "None")
                public String description;
            }

            static final class GetLoanProductsRescheduleStrategyType {

                private GetLoanProductsRescheduleStrategyType() {}

                @Schema(example = "3")
                public Integer id;
                @Schema(example = "loanRescheduleStrategyMethod.reduce.emi.amount")
                public String code;
                @Schema(example = "Reduce EMI amount")
                public String description;
            }

            public GetLoanProductsInterestRecalculationTemplateData.GetLoanProductsInterestRecalculationCompoundingType interestRecalculationCompoundingType;
            public GetLoanProductsInterestRecalculationTemplateData.GetLoanProductsRescheduleStrategyType rescheduleStrategyType;
            public GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsPreClosureInterestCalculationStrategy preClosureInterestCalculationStrategy;
        }

        static final class GetLoanProductsPaymentTypeOptions {

            private GetLoanProductsPaymentTypeOptions() {}

            @Schema(example = "10")
            public Integer id;
            @Schema(example = "check")
            public String name;
            @Schema(example = "1")
            public Integer position;
        }

        static final class GetLoanProductsCurrencyOptions {

            private GetLoanProductsCurrencyOptions() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetLoanProductsTransactionProcessingStrategyOptions {

            private GetLoanProductsTransactionProcessingStrategyOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "zeus-standard-strategy")
            public String code;
            @Schema(example = "Penalties, Fees, Interest, Principal order")
            public String name;
        }

        static final class GetLoanProductsChargeOptions {

            private GetLoanProductsChargeOptions() {}

            static final class GetLoanChargeTimeType {

                private GetLoanChargeTimeType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeTimeType.disbursement")
                public String code;
                @Schema(example = "Disbursement")
                public String description;
            }

            static final class GetLoanProductsChargeAppliesTo {

                private GetLoanProductsChargeAppliesTo() {}

                @Schema(example = "1  ")
                public Integer id;
                @Schema(example = "chargeAppliesTo.loan")
                public String code;
                @Schema(example = "Loan")
                public String description;
            }

            static final class GetLoanChargeCalculationType {

                private GetLoanChargeCalculationType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "chargeCalculationType.flat")
                public String code;
                @Schema(example = "Flat")
                public String description;
            }

            static final class GetLoansChargePaymentMode {

                private GetLoansChargePaymentMode() {}

                @Schema(example = "0")
                public Integer id;
                @Schema(example = "chargepaymentmode.regular")
                public String code;
                @Schema(example = "Regular")
                public String description;
            }

            @Schema(example = "5")
            public Integer id;
            @Schema(example = "des charges")
            public String name;
            @Schema(example = "true")
            public Boolean active;
            @Schema(example = "false")
            public Boolean penalty;
            public GetLoanProductsCurrencyOptions currency;
            @Schema(example = "100")
            public Long amount;
            public GetLoanChargeTimeType chargeTimeType;
            public GetLoanProductsChargeAppliesTo chargeAppliesTo;
            public GetLoanChargeCalculationType chargeCalculationType;
            public GetLoansChargePaymentMode chargePaymentMode;
        }

        static final class GetLoanProductsAccountingMappingOptions {

            private GetLoanProductsAccountingMappingOptions() {}

            static final class GetLoanProductsLiabilityAccountOptions {

                private GetLoanProductsLiabilityAccountOptions() {}

                static final class GetLoanProductsLiabilityType {

                    private GetLoanProductsLiabilityType() {}

                    @Schema(example = "2")
                    public Integer id;
                    @Schema(example = "accountType.liability")
                    public String code;
                    @Schema(example = "LIABILITY")
                    public String description;
                }

                static final class GetLoanProductsLiabilityUsage {

                    private GetLoanProductsLiabilityUsage() {}

                    @Schema(example = "1")
                    public Integer id;
                    @Schema(example = "accountUsage.detail")
                    public String code;
                    @Schema(example = "DETAIL")
                    public String description;
                }

                static final class GetLoanProductsLiabilityTagId {

                    private GetLoanProductsLiabilityTagId() {}

                    @Schema(example = "0")
                    public Integer id;
                }

                @Schema(example = "11")
                public Integer id;
                @Schema(example = "over payment")
                public String name;
                @Schema(example = "13")
                public Integer glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityUsage usage;
                @Schema(example = "over payment")
                public String nameDecorated;
                public GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "0")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsAssetAccountOptions {

                private GetLoanProductsAssetAccountOptions() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "Loan portfolio")
                public String name;
                @Schema(example = "02")
                public Integer glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "Loan portfolio")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "60000")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsExpenseAccountOptions {

                private GetLoanProductsExpenseAccountOptions() {}

                static final class GetLoanProductsExpenseType {

                    private GetLoanProductsExpenseType() {}

                    @Schema(example = "5")
                    public Integer id;
                    @Schema(example = "accountType.expense")
                    public String code;
                    @Schema(example = "EXPENSE")
                    public String description;
                }

                @Schema(example = "10")
                public Integer id;
                @Schema(example = "loans written off 2")
                public String name;
                @Schema(example = "12")
                public Integer glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsExpenseType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "loans written off 2")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "0")
                public Integer organizationRunningBalance;
            }

            static final class GetLoanProductsIncomeAccountOptions {

                private GetLoanProductsIncomeAccountOptions() {}

                static final class GetLoanProductsIncomeType {

                    private GetLoanProductsIncomeType() {}

                    @Schema(example = "4")
                    public Integer id;
                    @Schema(example = "accountType.income")
                    public String code;
                    @Schema(example = "INCOME")
                    public String description;
                }

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "income from interest")
                public String name;
                @Schema(example = "04")
                public Integer glCode;
                @Schema(example = "false")
                public Boolean disabled;
                @Schema(example = "true")
                public Boolean manualEntriesAllowed;
                public GetLoanProductsIncomeType type;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityUsage usage;
                @Schema(example = "income from interest")
                public String nameDecorated;
                public GetLoanProductsLiabilityAccountOptions.GetLoanProductsLiabilityTagId tagId;
                @Schema(example = "19")
                public Integer organizationRunningBalance;
            }

            public Set<GetLoanProductsLiabilityAccountOptions> liabilityAccountOptions;
            public Set<GetLoanProductsAssetAccountOptions> assetAccountOptions;
            public Set<GetLoanProductsExpenseAccountOptions> expenseAccountOptions;
            public Set<GetLoanProductsIncomeAccountOptions> incomeAccountOptions;
        }

        static final class GetLoanProductsValueConditionTypeOptions {

            private GetLoanProductsValueConditionTypeOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "zeus-standard-strategyLoanProductValueConditionType.equal")
            public String code;
            @Schema(example = "equals")
            public String description;
        }

        @Schema(example = "false")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "false")
        public Boolean useBorrowerCycle;
        public GetLoanProductsTemplateCurrency currency;
        public GetLoanProductsRepaymentTemplateFrequencyType repaymentFrequencyType;
        public GetLoanProductsInterestRateTemplateFrequencyType interestRateFrequencyType;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        public GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "[]")
        public List<Integer> principalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsTemplateResponse.GetLoanProductsAccountingRule accountingRule;
        public GetLoansProductsDaysInMonthTemplateType daysInMonthType;
        public GetLoanProductsDaysInYearTemplateType daysInYearType;
        @Schema(example = "false")
        public Boolean isInterestRecalculationEnabled;
        public GetLoanProductsInterestRecalculationTemplateData interestRecalculationData;
        public Set<GetLoanProductsPaymentTypeOptions> paymentTypeOptions;
        public Set<GetLoanProductsCurrencyOptions> currencyOptions;
        public Set<GetLoanProductsRepaymentTemplateFrequencyType> repaymentFrequencyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsPreClosureInterestCalculationStrategy> preClosureInterestCalculationStrategyOptions;
        public Set<GetLoanProductsInterestRateTemplateFrequencyType> interestRateFrequencyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsAmortizationType> amortizationTypeOptions;
        public Set<GetLoanProductsInterestTemplateType> interestTypeOptions;
        public Set<GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType> interestCalculationPeriodTypeOptions;
        public Set<GetLoanProductsTransactionProcessingStrategyOptions> transactionProcessingStrategyOptions;
        public Set<GetLoanProductsChargeOptions> chargeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsAccountingRule> accountingRuleOptions;
        public GetLoanProductsAccountingMappingOptions accountingMappingOptions;
        public Set<GetLoanProductsValueConditionTypeOptions> valueConditionTypeOptions;
        public Set<GetLoansProductsDaysInMonthTemplateType> daysInMonthTypeOptions;
        public Set<GetLoanProductsInterestTemplateType> daysInYearTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingType> interestRecalculationCompoundingTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsRescheduleStrategyType> rescheduleStrategyTypeOptions;
        public Set<GetLoanProductsResponse.GetLoanProductsInterestRecalculationData.GetLoanProductsInterestRecalculationCompoundingFrequencyType> interestRecalculationFrequencyTypeOptions;
    }

    @Schema(description = "GetLoanProductsProductIdResponse")
    public static final class GetLoanProductsProductIdResponse {

        private GetLoanProductsProductIdResponse() {}

        static final class GetLoanProductsInterestRateFrequencyType {

            private GetLoanProductsInterestRateFrequencyType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "interestRateFrequency.periodFrequencyType.months")
            public String code;
            @Schema(example = "Per month")
            public String description;
        }

        static final class GetLoanProductsPrincipalVariationsForBorrowerCycle {

            private GetLoanProductsPrincipalVariationsForBorrowerCycle() {}

            static final class GetLoanProductsParamType {

                private GetLoanProductsParamType() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "LoanProductParamType.principal")
                public String code;
                @Schema(example = "principal")
                public String description;
            }

            static final class  GetLoanProductsValueConditionType {

                private GetLoanProductsValueConditionType() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "LoanProductValueConditionType.equal")
                public String code;
                @Schema(example = "equals")
                public String description;
            }

            @Schema(example = "21")
            public Integer id;
            @Schema(example = "1")
            public Integer borrowerCycleNumber;
            public GetLoanProductsParamType paramType;
            public GetLoanProductsValueConditionType valueConditionType;
            @Schema(example = "2000.000000")
            public Float minValue;
            @Schema(example = "20000.000000")
            public Float maxValue;
            @Schema(example = "15000.000000")
            public Float defaultValue;
        }

        static final class GetLoanAccountingMappings {

            private GetLoanAccountingMappings() {}

            static final class GetLoanFundSourceAccount {

                private GetLoanFundSourceAccount() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "fund source")
                public String name;
                @Schema(example = "01")
                public Integer glCode;
            }

            static final class GetLoanPortfolioAccount {

                private GetLoanPortfolioAccount() {}

                @Schema(example = "2")
                public Integer id;
                @Schema(example = "Loan portfolio")
                public String name;
                @Schema(example = "02")
                public Integer glCode;
            }

            static final class GetLoanTransfersInSuspenseAccount {

                private GetLoanTransfersInSuspenseAccount() {}

                @Schema(example = "3")
                public Integer id;
                @Schema(example = "transfers")
                public String name;
                @Schema(example = "03")
                public Integer glCode;
            }

            static final class GetLoanInterestOnLoanAccount {

                private GetLoanInterestOnLoanAccount() {}

                @Schema(example = "4")
                public Integer id;
                @Schema(example = "income from interest")
                public String name;
                @Schema(example = "04")
                public Integer glCode;
            }

            static final class GetLoanIncomeFromFeeAccount {

                private GetLoanIncomeFromFeeAccount() {}

                @Schema(example = "8")
                public Integer id;
                @Schema(example = "income from fees 2")
                public String name;
                @Schema(example = "10")
                public Integer glCode;
            }

            static final class GetLoanIncomeFromPenaltyAccount {

                private GetLoanIncomeFromPenaltyAccount() {}

                @Schema(example = "9")
                public Integer id;
                @Schema(example = "income from penalities 2")
                public String name;
                @Schema(example = "11")
                public Integer glCode;
            }

            static final class GetLoanWriteOffAccount {

                private GetLoanWriteOffAccount() {}

                @Schema(example = "10")
                public Integer id;
                @Schema(example = "loans written off 2")
                public String name;
                @Schema(example = "12")
                public Integer glCode;
            }

            static final class GetLoanOverpaymentLiabilityAccount {

                private GetLoanOverpaymentLiabilityAccount() {}

                @Schema(example = "11")
                public Integer id;
                @Schema(example = "over payment")
                public String name;
                @Schema(example = "13")
                public Integer glCode;
            }

            public GetLoanFundSourceAccount fundSourceAccount;
            public GetLoanPortfolioAccount loanPortfolioAccount;
            public GetLoanTransfersInSuspenseAccount transfersInSuspenseAccount;
            public GetLoanInterestOnLoanAccount interestOnLoanAccount;
            public GetLoanIncomeFromFeeAccount incomeFromFeeAccount;
            public GetLoanIncomeFromPenaltyAccount incomeFromPenaltyAccount;
            public GetLoanWriteOffAccount writeOffAccount;
            public GetLoanOverpaymentLiabilityAccount overpaymentLiabilityAccount;
        }

        static final class GetLoanPaymentChannelToFundSourceMappings {

            private GetLoanPaymentChannelToFundSourceMappings() {}

            static final class GetLoanPaymentType {

                private GetLoanPaymentType() {}

                @Schema(example = "10")
                public Integer id;
                @Schema(example = "check")
                public String name;
            }

            public GetLoanPaymentType paymentType;
            public GetLoanAccountingMappings.GetLoanFundSourceAccount fundSourceAccount;
        }

        static final class GetLoanFeeToIncomeAccountMappings {

            private GetLoanFeeToIncomeAccountMappings() {}

            static final class GetLoanCharge {

                private GetLoanCharge() {}

                @Schema(example = "1")
                public Integer id;
                @Schema(example = "flat install")
                public String name;
                @Schema(example = "false")
                public Boolean active;
                @Schema(example = "false")
                public Boolean penalty;
            }

            public GetLoanCharge charge;
            public GetLoanAccountingMappings.GetLoanIncomeFromFeeAccount incomeAccount;
        }

        @Schema(example = "11")
        public Integer id;
        @Schema(example = "advanced accounting")
        public String name;
        @Schema(example = "ad11")
        public String shortName;
        @Schema(example = "true")
        public Boolean includeInBorrowerCycle;
        @Schema(example = "true")
        public Boolean useBorrowerCycle;
        @Schema(example = "loanProduct.active")
        public String status;
        public GetLoanProductsResponse.GetLoanProductsCurrency currency;
        @Schema(example = "10000.000000")
        public Float principal;
        @Schema(example = "2000.000000")
        public Float minPrincipal;
        @Schema(example = "15000.000000")
        public Float maxPrincipal;
        @Schema(example = "7")
        public Integer numberOfRepayments;
        @Schema(example = "7")
        public Integer repaymentEvery;
        public GetLoanProductsResponse.GetLoanProductsRepaymentFrequencyType repaymentFrequencyType;
        @Schema(example = "5.000000")
        public Float interestRatePerPeriod;
        public GetLoanProductsProductIdResponse.GetLoanProductsInterestRateFrequencyType interestRateFrequencyType;
        @Schema(example = "60.000000")
        public Float annualInterestRate;
        public GetLoanProductsResponse.GetLoanProductsAmortizationType amortizationType;
        @Schema(example = "5.5")
        public BigDecimal fixedPrincipalPercentagePerInstallment;
        public GetLoanProductsTemplateResponse.GetLoanProductsInterestTemplateType interestType;
        public GetLoanProductsResponse.GetLoansProductsInterestCalculationPeriodType interestCalculationPeriodType;
        @Schema(example = "1")
        public Integer transactionProcessingStrategyId;
        @Schema(example = "Zeus style")
        public String transactionProcessingStrategyName;
        @Schema(example = "[]")
        public List<Integer> charges;
        public Set<GetLoanProductsPrincipalVariationsForBorrowerCycle> productsPrincipalVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> interestRateVariationsForBorrowerCycle;
        @Schema(example = "[]")
        public List<Integer> numberOfRepaymentVariationsForBorrowerCycle;
        public GetLoanProductsResponse.GetLoanProductsAccountingRule accountingRule;
        public GetLoanAccountingMappings accountingMappings;
        public Set<GetLoanPaymentChannelToFundSourceMappings> paymentChannelToFundSourceMappings;
        public Set<GetLoanFeeToIncomeAccountMappings> feeToIncomeAccountMappings;
        @Schema(example = "true")
        public Boolean multiDisburseLoan;
        @Schema(example = "3")
        public Integer maxTrancheCount;
        @Schema(example = "36000.000000")
        public Float outstandingLoanBalance;
        @Schema(example = "2")
        public Integer overdueDaysForNPA;
        @Schema(example = "50")
        public Integer principalThresholdForLastInstalment;
    }

    @Schema(description = "PutLoanProductsProductIdRequest")
    public static final class PutLoanProductsProductIdRequest {

        private PutLoanProductsProductIdRequest() {}

        @Schema(example = "NOCPL PRODUCT")
        public String name;

        @Schema(example = "LPAA")
        public String shortName;

        @Schema(example = "CONOCPL")
        public String loanAccNoPreference;

        @Schema(example = "Loan Product Creation")
        public  String description;

        @Schema(example = "Actual")
        public  String brokenInterestCalculationPeriod;

        @Schema(example = "Strategy1")
        public  String repaymentStrategyForNpaId;

        @Schema(example = "1")
        public  String brokenInterestStrategy;

        @Schema(example = "false")
        public  boolean useDaysInMonthForLoanProvisioning;

        @Schema(example = "false")
        public  boolean divideByThirtyForPartialPeriod;

        @Schema(example = "true")
        public  boolean allowApprovalOverAmountApplied;

        @Schema(example = "true")
        public  boolean includeInBorrowerCycle;

        @Schema(example = "true")
        public  boolean useBorrowerCycle;

        @Schema(example = "10 March 2023")
        public  LocalDate startDate;

        @Schema(example = "1")
        public Integer principalThresholdForLastInstallment;

        @Schema(example = "10 March 2024")
        public  LocalDate closeDate;

        @Schema(example = "10000.00")
        public   BigDecimal principal;

        @Schema(example = "1")
        public  BigDecimal minPrincipal;

        @Schema(example = "100000000")
        public  BigDecimal maxPrincipal;

        @Schema(example = "12")
        public  Integer numberOfRepayments;

        @Schema(example = "1")
        public  Integer minNumberOfRepayments;

        @Schema(example = "24")
        public  Integer maxNumberOfRepayments;

        @Schema(example = "1")
        public  Integer repaymentEvery;

        @Schema(example = "2")
        public Integer repaymentFrequencyType;

        @Schema(example = "12")
        public  BigDecimal interestRatePerPeriod;

        @Schema(example = "12")
        public  BigDecimal minInterestRatePerPeriod;

        @Schema(example = "36")
        public  BigDecimal maxInterestRatePerPeriod;

        @Schema(example = "3")
        public  Integer interestRateFrequencyType;

        @Schema(example = "false")
        public  boolean isLinkedToFloatingInterestRates;

        @Schema(example = "false")
        public  boolean allowVariableInstallments;

        @Schema(example = "true")
        public  boolean enableColendingLoan;

        @Schema(example = "true")
        public  boolean byPercentageSplit;

        @Schema(example = "100")
        public  Integer selfPrincipalShare;

        @Schema(example = "100")
        public  Integer selfFeeShare;

        @Schema(example = "")
        public  Integer selfPenaltyShare;

        @Schema(example = "")
        public  Integer selfOverpaidShares;

        @Schema(example = "100")
        public  BigDecimal selfInterestRate;

        @Schema(example = "100")
        public  Integer principalShare;

        @Schema(example = "100")
        public  Integer feeShare;

        @Schema(example = "100")
        public  Integer penaltyShare;

        @Schema(example = "100")
        public  Integer overpaidShare;

        @Schema(example = "100")
        public  BigDecimal interestRate;

        @Schema(example = "0")
        public  Integer partnerPrincipalShare;

        @Schema(example = "0")
        public  Integer partnerFeeShare;

        @Schema(example = "")
        public  Integer partnerPenaltyShare;

        @Schema(example = "")
        public  Integer partnerOverpaidShare;

        @Schema(example = "0")
        public  BigDecimal partnerInterestRate;

        @Schema(example = "1")
        public  Long partnerId;

        @Schema(example = "false")
        public  boolean enableFeesWiseBifacation;

        @Schema(example = "false")
        public  boolean enableChargeWiseBifacation;

        @Schema(example = "true")
        public  boolean enableOverDue;

        @Schema(example = "10")
        public  Integer colendingCharge;

        @Schema(example = "100")
        public  Integer selfCharge;

        @Schema(example = "0")
        public  Integer partnerCharge;

        @Schema(example = "false")
        public  boolean selectAcceptedDates;

        @Schema(example = "false")
        public  boolean applyPrepaidLockingPeriod;

        @Schema(example = "false")
        public  boolean applyForeclosureLockingPeriod;

        @Schema(example = "1")
        public  Integer amortizationType;

        @Schema(example = "0")
        public  Integer interestType;

        @Schema(example = "0")
        public  Integer interestCalculationPeriodType;

        @Schema(example = "")
        public  BigDecimal inArrearsTolerance;

        @Schema(example = "1")
        public  Integer daysInMonthType;

        @Schema(example = "1")
        public  Integer daysInYearType;

        @Schema(example = "false")
        public  boolean isInterestRecalculationEnabled;

        @Schema(example = "45")
        public  Integer minimumDaysBetweenDisbursalAndFirstRepayment;

        @Schema(example = "50")
        public  BigDecimal gstLiabilityByVcpl;

        @Schema(example = "50")
        public  BigDecimal gstLiabilityByPartner;

        @Schema(example = "1")
        public  Integer transactionTypePreference;

        @Schema(example = "100")
        public  BigDecimal vcplShareInBrokenInterest;

        @Schema(example = "0")
        public  BigDecimal partnerShareInBrokenInterest;

        @Schema(example = "12")
        public  Integer monitoringTriggerPar30;

        @Schema(example = "12")
        public  Integer monitoringTriggerPar90;

        @Schema(example = "69")
        public  Integer fldgLogic;

        @Schema(example = "85")
        public  Integer assetClass;

        @Schema(example = "")
        public Collection<ChargeData> charges;

        @Schema(example = "1")
        public  Integer accountingRule;

        @Schema(example = "false")
        public  boolean isEqualAmortization;

        @Schema(example = "100896757425")
        public  Long disbursementAccountNumber;

        @Schema(example = "657465736878")
        public  Long collectionAccountNumber;

        @Schema(example = "true")
        public  Boolean isPennyDropEnabled;

        @Schema(example = "true")
        public  Boolean isBankDisbursementEnabled;

        @Schema(example = "true")
        public  Boolean servicerFeeInterestConfigEnabled;

        @Schema(example = "true")
        public  Boolean servicerFeeChargesConfigEnabled;
        @Schema(example = "true")
        public  boolean enableDedupe;
        @Schema(example = "PAN/AADHAAR")
        public Integer dedupeType;
        @Schema(example = "bank-account-key")
        public Integer disbursementBankAccountName;

        @Schema(example = "INR")
        public String currencyCode;

        @Schema(example = "en_GB")
        public String locale;

        @Schema(example = "2")
        public Integer digitsAfterDecimal;

        @Schema(example = "UP")
        public String emiRoundingMode;

        @Schema(example = "1")
        public Integer emimultiples;

        @Schema(example = "UP")
        public String interestRoundingMode;

        @Schema(example = "1")
        public Integer interestDecimal;

        @Schema(example = "1")
        public Integer interestDecimalRegex;

        @Schema(example = "0")
        public Integer emiDecimalRegex;

        @Schema(example = "Loan Strategy1")
        public String loanForeclosureStrategy;

        @Schema(example = "2")
        public String brokenInterestDaysInYears;

        @Schema(example = "1")
        public Long transactionProcessingStrategyId;

        @Schema(example = "1")
        public Long graceOnPrincipalPayment;

        @Schema(example = "10")
        public Integer graceOnInterestPayment;

        @Schema(example = "1")
        public Integer graceOnInterestCharged;

        @Schema(example = "1")
        public String repaymentStrategyForNpa;

        @Schema(example = "false")
        public boolean canDefineInstallmentAmount;

        @Schema(example = "1")
        public Integer graceOnArrearsAgeing;

        @Schema(example = "1")
        public Integer overdueDaysForNPA;

        @Schema(example = "false")
        public Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;

        @Schema(example = "1002")
        public BigDecimal aumSlabRate;

        @Schema(example = "false")
        public Boolean disallowExpectedDisbursements;

        @Schema(example = "false")
        public Boolean canUseForTopup;

        @Schema(example = "false")
        public Boolean holdGuaranteeFunds;

        @Schema(example = "false")
        public Boolean multiDisburseLoan;

        @Schema(example = "129")
        public Integer insuranceApplicability;

        @Schema(example = "1")
        public Integer disbursement;

        @Schema(example = "1")
        public Integer collection;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

    }

    @Schema(description = "PutLoanProductsProductIdResponse")
    public static final class PutLoanProductsProductIdResponse {

        private PutLoanProductsProductIdResponse() {}

        static final class PutLoanChanges {

            private PutLoanChanges() {}

        }

        @Schema(example = "1")
        public Integer resourceId;

        public PutLoanChanges changes;
    }
}
