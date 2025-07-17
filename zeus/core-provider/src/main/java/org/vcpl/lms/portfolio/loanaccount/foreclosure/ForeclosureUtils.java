package org.vcpl.lms.portfolio.loanaccount.foreclosure;

import org.jetbrains.annotations.NotNull;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.common.domain.DaysInYearType;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import org.vcpl.lms.portfolio.loanaccount.exception.LoanForeclosureException;
import org.vcpl.lms.portfolio.loanproduct.domain.ForeclosureMethodTypes;
import org.vcpl.lms.portfolio.loanproduct.domain.ForeclosurePos;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProductFeesCharges;
import org.vcpl.lms.portfolio.loanproduct.domain.ProductCollectionConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;

public final class ForeclosureUtils {
    private ForeclosureUtils(){
    }
    public static LoanRepaymentScheduleInstallment getTheForeclosureInstallment(Loan loan, LocalDate foreClosureDate ){
        List<LoanRepaymentScheduleInstallment> installments =  retrieveListOfInstallments(loan,foreClosureDate);
        return installments.isEmpty() ? loan.fetchRepaymentScheduleInstallment(1):  installments.get(installments.size()-1);
    }
    public static List<LoanRepaymentScheduleInstallment> retrieveListOfInstallments(Loan loan,LocalDate foreClosureDate) {
        return  loan.getRepaymentScheduleInstallments().stream()
                .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isBefore(foreClosureDate)
                        || loanRepaymentScheduleInstallment.getDueDate().isEqual(foreClosureDate)).toList();
    }
    public static double foreclosureInterest( BigDecimal totalPrincipalOutstanding,
                                              BigDecimal annualNominalInterestRate, int days){
        return  totalPrincipalOutstanding.doubleValue() * (annualNominalInterestRate.doubleValue() / 100)  * days;

    }

    public   static  int tillDays(LoanRepaymentScheduleInstallment currentInstallment,LocalDate paymentDate){
        return Math.toIntExact(ChronoUnit.DAYS.between(currentInstallment.getDueDate(),paymentDate));
    }

    public static double calculateForeclosureInterest(int daysInYear, BigDecimal totalPrincipalOutstanding,
                                                      BigDecimal annualNominalInterestRate, int days,LocalDate foreclosureDate){
        DaysInYearType daysInYearType = DaysInYearType.fromInt(daysInYear);
        double daysInYearValue = daysInYearType.getValue();
        if(daysInYearType.isActual()){
            return ForeclosureUtils.foreclosureInterest( totalPrincipalOutstanding, annualNominalInterestRate, days) / foreclosureDate.lengthOfYear();
        }
        return ForeclosureUtils.foreclosureInterest( totalPrincipalOutstanding, annualNominalInterestRate, days)  / daysInYearValue;
    }

    public static  double selfInterest(double interestForCurrentPeriod,   Loan loan){
        final BigDecimal selfSplitShare = BigDecimal.valueOf(loan.getLoanProduct().getSelfPrincipalShare());
        final BigDecimal selfShare = loan.getLoanProduct().getSelfInterestRate();
        final BigDecimal interestRate = loan.getLoanProductRelatedDetail().getAnnualNominalInterestRate();
        final BigDecimal selfInterestRate = interestRate.divide(BigDecimal.valueOf(100)).multiply(selfShare);
        return  interestForCurrentPeriod * (selfInterestRate.doubleValue() / interestRate.doubleValue()) * (selfSplitShare.doubleValue() / 100);
    }

    public static double partnerInterest(double interestForCurrentPeriod, double selfInterest){
        return interestForCurrentPeriod - selfInterest;
    }

    public static Money retrieveOverDueInterest(List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments, LocalDate transactionDate, MonetaryCurrency currency) {

        return loanRepaymentScheduleInstallments.stream()
                .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isBefore(transactionDate)
                        && !loanRepaymentScheduleInstallment.isObligationsMet())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }

    public static Money retrieveOverDueSelfInterest(List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments, LocalDate transactionDate, MonetaryCurrency currency) {

        return loanRepaymentScheduleInstallments.stream()
                .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isBefore(transactionDate)
                        && !loanRepaymentScheduleInstallment.isObligationsMet())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }
    public static Money retrieveOverDuePartnerInterest(List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments, LocalDate transactionDate, MonetaryCurrency currency) {

        return loanRepaymentScheduleInstallments.stream()
                .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isBefore(transactionDate)
                        && !loanRepaymentScheduleInstallment.isObligationsMet())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }

    public static Money retrieveInterestOutstanding(List<LoanRepaymentScheduleInstallment> newInstallments,MonetaryCurrency currency) {

        return newInstallments.stream().
                filter(loanRepaymentScheduleInstallment -> !loanRepaymentScheduleInstallment.isObligationsMet()
                        || loanRepaymentScheduleInstallment.isPartlyPaid())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }

    public static Money retrieveSelfInterestOutstanding(List<LoanRepaymentScheduleInstallment> newInstallments,MonetaryCurrency currency ) {
        return  newInstallments.stream().
                filter(loanRepaymentScheduleInstallment -> !loanRepaymentScheduleInstallment.isObligationsMet()
                        || loanRepaymentScheduleInstallment.isPartlyPaid())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getSelfInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }

    public static Money retrievePartnerInterestOutstanding(List<LoanRepaymentScheduleInstallment> newInstallments,MonetaryCurrency currency ) {
        return newInstallments.stream().
                filter(loanRepaymentScheduleInstallment -> !loanRepaymentScheduleInstallment.isObligationsMet()
                        || loanRepaymentScheduleInstallment.isPartlyPaid())
                .map(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getPartnerInterestOutstanding(currency))
                .reduce(Money.zero(currency),Money::add);
    }

    public static Boolean isAdvance(List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments) {
        return loanRepaymentScheduleInstallments
                .stream()
                .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getTotalPaidInAdvance().doubleValue() > 0)
                .map(LoanRepaymentScheduleInstallment::getTotalPaidInAdvance)
                .reduce(BigDecimal.ZERO,BigDecimal::add).doubleValue()>0;
    }


    public static BigDecimal retrieveForeclosureSelfShare(Set<LoanProductFeesCharges> loanProductFeesCharges){

        return   loanProductFeesCharges.stream()
                .filter(charges -> charges.getCharge().isForeclosureCharge())
                .map(LoanProductFeesCharges::getSelfShare)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }



    public static BigDecimal retrieveSelfForeclosureFeeCharge(BigDecimal feeChargePortion , BigDecimal foreclosureSelfShare,BigDecimal divisor) {

        return feeChargePortion.multiply(foreclosureSelfShare).divide(divisor, RoundingMode.CEILING);
    }

    public static Money retrievePartnerForeclosureFeeCharge(Money feeChargesPortion, BigDecimal selfFeeChargesPortion) {
        return feeChargesPortion.minus(selfFeeChargesPortion);
    }
    public static void addLoanChargesPaidBy(LoanTransaction loanTransaction, Loan loan) {
        Set<LoanChargePaidBy> loanChargePaidBySet = new HashSet<>();
        /* Retrieve a foreclosure charge from the Loan */
        LoanCharge loanCharge = loan.getLoanCharges().stream().filter(LoanCharge::isForeclosureCharge).findAny().orElse(null);
        if(Objects.nonNull(loanCharge)){
        LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(loanTransaction,loanCharge,loanCharge.getAmount(),loan.fetchRepaymentScheduleInstallment(loan.getRepaymentScheduleInstallments().size()).getInstallmentNumber());
        loanChargePaidBySet.add(loanChargePaidBy);}
        loanTransaction.setLoanChargesPaid(loanChargePaidBySet);
    }

    public static Money getRemainingInterestOutstanding(List<LoanRepaymentScheduleInstallment> listOfInstallment,MonetaryCurrency currency,LocalDate transactionDate) {

       return listOfInstallment.stream().filter(installment -> installment.getInterestOutstanding(currency).isGreaterThanZero())
                .map(installment -> installment.getInterestOutstanding(currency)).reduce(Money.zero(currency),Money::add);
    }

    public static Money getRemainingSelfInterestOutstanding(List<LoanRepaymentScheduleInstallment> listOfInstallment, MonetaryCurrency currency) {
        return listOfInstallment.stream().filter(installment -> installment.getInterestOutstanding(currency).isGreaterThanZero())
                .map(installment -> installment.getSelfInterestOutstanding(currency)).reduce(Money.zero(currency),Money::add);
    }

    public static Money getRemainingPartnerInterestOutstanding(List<LoanRepaymentScheduleInstallment> listOfInstallment, MonetaryCurrency currency) {
        return listOfInstallment.stream().filter(installment -> installment.getInterestOutstanding(currency).isGreaterThanZero())
                .map(installment -> installment.getPartnerInterestOutstanding(currency)).reduce(Money.zero(currency),Money::add);
    }

    public static BigDecimal getForeclosureInterestCalPos(Integer foreclosurePos,LoanRepaymentScheduleInstallment installment,Loan loan, ForeclosureEnum foreclosureEnum){
        return Boolean.TRUE.equals(ForeclosurePos.isRsPos(ForeclosurePos.fromInt(checkForeclosurePosStrategyIsNull(foreclosurePos,foreclosureEnum)))) ? installment.getPrincipalOutstanding() : loan.getLoanSummary().getTotalPrincipalOutstanding();
    }

    public static BigDecimal getForeclosureChargeCalculationPos(Integer foreclosureChargeEnum, BigDecimal principalAmount, Loan loan,ForeclosureEnum foreclosureEnum) {
        return Boolean.TRUE.equals(ForeclosurePos.isRsPos(ForeclosurePos.fromInt(checkForeclosurePosStrategyIsNull(foreclosureChargeEnum,foreclosureEnum)))) ? principalAmount : loan.getLoanSummary().getTotalPrincipalOutstanding();
    }

    public static boolean isPrincipalInterestOutstanding(ForeclosureMethodTypes foreclosureMethodTypes){
        return ForeclosureMethodTypes.isPrincipalInterestOutstanding(foreclosureMethodTypes);
    }

    public static boolean isPrincipalOutstandingInterestOverdue(Integer value){
        if(Objects.isNull(value))
            return Boolean.FALSE;
        return ForeclosureMethodTypes.isPrincipalOutstandingInterestDue(ForeclosureMethodTypes.fromInt(value));
    }
    public static boolean isPrincipalOutstandingInterestAccrued(ForeclosureMethodTypes foreclosureMethodTypes){
        return ForeclosureMethodTypes.isPrincipalOutstandingInterestAccrued(foreclosureMethodTypes);
    }
    public static BigDecimal total(BigDecimal ... amount){
        if (amount.length == 0) {
            throw new IllegalArgumentException("amount array must not be empty");
        }
        BigDecimal total = amount[0];
        for (int i = 1; i < amount.length; i++) {
            total = total.add(amount[i]);
        }
        return total;

    }

    public static void getAdvanceAmountForTransaction(Loan loan,List<LoanTransaction> loanTransactions) {

        Predicate<LoanTransaction> checkAdvance = loanTransaction->
                Objects.nonNull(loanTransaction.getEvent()) && loanTransaction.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.UNPROCESSEDADVANCE.getValue())
                        && loanTransaction.getAdvanceAmount().doubleValue()> 0;

        loan.getLoanTransactions().stream().filter(checkAdvance)
                .map(loanTransaction -> {
                    LoanTransaction transaction = LoanTransaction.copyTransactionProperties(loanTransaction);
                    transaction.resetDerivedComponents();
                    loanTransaction.setEvent(BusinessEventNotificationConstants.BusinessEvents.PROCESSEDADVANCE.getValue());
                    loanTransaction.setAdvanceAmountprocessed(1L);
                    transaction.setAmount(loanTransaction.getAdvanceAmount());
                    transaction.setParentId(loanTransaction.getId());
                    return transaction;})
                .forEach(loanTransactions::add);


    }
    public static BigDecimal getBounceCharge(Loan loan,Integer loanRepaymentScheduleInstallment) {

        BigDecimal bounceCharge = BigDecimal.ZERO;

        for (LoanCharge loanCharge : loan.getLoanCharges()) {
            if (loanCharge.isBounceCharge() && loanCharge.getInstallmentNumber() <= loanRepaymentScheduleInstallment
                    && loanCharge.isActive() && !loanCharge.isFullyPaid()) {
                bounceCharge = bounceCharge.add(loanCharge.amount().add(loanCharge.getTotalGst()));
            }
        }
        return bounceCharge;
    }

    public static Long getBounceCharge(Loan loan) {

        return loan.getLoanProduct().getLoanProductFeesCharges()
                .stream()
                .filter(loanProductFeesCharges -> loanProductFeesCharges.getCharge().isBounceCharge())
                .map(loanProductFeesCharges -> loanProductFeesCharges.getCharge().getId())
                .findAny()
                .orElseGet(() -> {
                    throw new InvalidLoanTransactionTypeException("Bounce Charge", "Bounce Charge Not Found For Loan Id ", String.valueOf(loan.getId()));
                });
    }
    public static LocalDate convert(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static boolean checkIsForeclosureType(Object nullCheckValue){
        return !isNullValue(nullCheckValue);
    }
    public static boolean isNullValue(Object nullCheckValue){
        return Objects.isNull(nullCheckValue);
    }
    public static BigDecimal getInterestAmountBasedOnForeclosureType(int foreClosureType, Loan loan, LocalDate foreclosureDate, LoanAccrualRepository loanAccrualRepository, boolean isToLoadForeclosureTemplate) {
        BigDecimal interestResult;
        ForeclosureMethodTypes foreclosureMethodType = ForeclosureMethodTypes.fromInt(foreClosureType);
        switch (foreclosureMethodType) {
            case PRINCIPAL_OUTSTANDING_INTEREST_OUTSTANDING ->
                    interestResult = loan.getSummary().getTotalInterestOutstanding();
            case PRINCIPAL_OUTSTANDING_INTEREST_DUE -> {
                LoanRepaymentScheduleInstallment nextInstallment = ForeclosureUtils.getNextInstallment(loan, foreclosureDate);
                interestResult = nextInstallment.getInterestCharged();
            }
            case PRINCIPAL_OUTSTANDING_INTEREST_ACCRUED -> {
                LocalDate accrualDate = foreclosureDate.minusDays(1);
                Optional<LoanAccrual> currentAccrualRecord = loanAccrualRepository.getLoanAccrualsByLoanIdAndFromDate(loan.getId(), DateUtils.convertLocalDateToDate(accrualDate));
                if (currentAccrualRecord.isPresent()) {
                    LoanAccrual loanAccrual = currentAccrualRecord.get();
                    interestResult = loanAccrual.getInterestAccruedButNotDue().add(getOverdueInterestCharge(loan.getRepaymentScheduleInstallments(),foreclosureDate));
                } else {
                    if (isToLoadForeclosureTemplate)
                        interestResult = BigDecimal.ZERO;
                    else {
                        loan.validateForForeclosure(foreclosureDate);
                        //if accrual not found exception message is thrown
                        final String defaultUserMessage = "For ".concat(foreclosureMethodType.toString().concat(" foreclosure method type, unable to calculate the interest"));
                        throw new LoanForeclosureException("loan.with.interest.recalculation.enabled.cannot.be.foreclosure", defaultUserMessage);
                    }
                }
            }
            default -> interestResult = BigDecimal.ZERO;
        }
        return interestResult;
    }

    public static LoanRepaymentScheduleInstallment getNextInstallment(Loan loan, LocalDate foreClosureDate) {
        List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = loan.getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : repaymentScheduleInstallments) {
            if (getLoanRepaymentScheduleInstallmentPredicate(foreClosureDate).test(loanRepaymentScheduleInstallment)) {
                return loanRepaymentScheduleInstallment;
            }
        }
        return repaymentScheduleInstallments.get(repaymentScheduleInstallments.size() - 1);
    }

    @NotNull
    private static Predicate<LoanRepaymentScheduleInstallment> getLoanRepaymentScheduleInstallmentPredicate(LocalDate foreClosureDate) {
        return loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isAfter(foreClosureDate)
                || loanRepaymentScheduleInstallment.getDueDate().isEqual(foreClosureDate);
    }

    public static BigDecimal getForeclosurePrincipalBasedOnMethod(Integer foreclosurePos,LoanRepaymentScheduleInstallment installment,Loan loan){
        return loan.getLoanSummary().getTotalPrincipalOutstanding();
    }

    private static Integer checkForeclosurePosStrategyIsNull(Integer selectedMethod,ForeclosureEnum foreclosureEnum){
        if(Objects.isNull(selectedMethod)){
            final String defaultUserMessage = "In product, for ".concat(foreclosureEnum.toString()).concat(" scenario select the principal outstanding strategy ");
            throw new LoanForeclosureException("loan.with.interest.recalculation.enabled.cannot.be.foreclosured", defaultUserMessage);
        }
        return selectedMethod;
    }

    public static LoanRepaymentScheduleInstallment getNextInstallment(LoanRepaymentScheduleInstallment installments){

        return  installments.getLoan().fetchRepaymentScheduleInstallment(Boolean.TRUE.equals(installments.isLastInstallment(installments.getLoan().getNumberOfRepayments(),installments.getInstallmentNumber()))
                ? installments.getInstallmentNumber()
                : installments.getInstallmentNumber() +1);

    }

    public static LoanRepaymentScheduleInstallment getPreviousInstallment(LoanRepaymentScheduleInstallment installments){

        return   installments.getInstallmentNumber() ==1 ?
                installments :installments.getLoan().fetchRepaymentScheduleInstallment(installments.getInstallmentNumber() - 1);
    }

    public  static boolean isNeedToAppropriateTheInterest(Integer methodType){
        if(Objects.isNull(methodType)) return Boolean.FALSE;
        ForeclosureMethodTypes foreclosureMethodTypes= ForeclosureMethodTypes.fromInt(methodType);
        return ForeclosureUtils.isPrincipalInterestOutstanding(foreclosureMethodTypes) || ForeclosureUtils.isPrincipalOutstandingInterestAccrued(foreclosureMethodTypes);
    }

    public static BigDecimal getOverdueInterestCharge(List<LoanRepaymentScheduleInstallment> newInstallments,LocalDate foreclosureDate) {

        return newInstallments.stream().
                filter(loanRepaymentScheduleInstallment -> !loanRepaymentScheduleInstallment.isObligationsMet() && (loanRepaymentScheduleInstallment.getDueDate().isBefore(foreclosureDate)))
                .map(LoanRepaymentScheduleInstallment::getInterestCharged)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}