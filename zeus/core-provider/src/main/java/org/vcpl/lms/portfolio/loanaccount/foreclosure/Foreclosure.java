package org.vcpl.lms.portfolio.loanaccount.foreclosure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.organisation.monetary.domain.Money;
import org.vcpl.lms.portfolio.charge.domain.*;
import org.vcpl.lms.portfolio.collection.data.PrincipalAppropriationData;
import org.vcpl.lms.portfolio.collection.service.CollectionAppropriation;
import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;
import org.vcpl.lms.portfolio.loanaccount.data.GstData;
import org.vcpl.lms.portfolio.loanaccount.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.loanHistory.LoanHistoryRepo;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistory;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.domain.LoanRepaymentScheduleHistoryRepository;
import org.vcpl.lms.portfolio.loanaccount.service.GstService;
import org.vcpl.lms.portfolio.loanaccount.service.GstServiceImpl;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.service.ServicerFeeAmountFormulaCalculation;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.portfolio.loanproduct.domain.ProductCollectionConfig;
import org.vcpl.lms.useradministration.domain.AppUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public interface Foreclosure   {

    Logger LOG = LoggerFactory.getLogger(Foreclosure.class);
    void foreclosureAppropriation(List<LoanRepaymentScheduleInstallment> installments, Loan loan, LoanTransaction loanTransaction, LocalDate transactionDate,
                                  LoanHistoryRepo loanHistoryRepo, LoanProduct loanProduct);



    default ForeclosureEnum checkForeclosureScenario(LoanRepaymentScheduleInstallment installments, LocalDate foreClosureDate, MonetaryCurrency currency, Loan loan,ProductCollectionConfig productCollectionConfig) {

        LoanRepaymentScheduleInstallment nextInstallment = ForeclosureUtils.getNextInstallment(installments);
        LoanRepaymentScheduleInstallment previousInstallment =ForeclosureUtils.getPreviousInstallment(installments);
        /* Checking the loan advance amount */
        Boolean isAdvance =  ForeclosureUtils.isAdvance(loan.getRepaymentScheduleInstallments());
        if(ForeclosureUtils.checkIsForeclosureType(productCollectionConfig.getForeclosureMethodType()))
            return ForeclosureEnum.FORECLOSURE_METHODS;
        /*Normal foreclosure scenario: The foreclosure date is before due date */


         if((installments.getInstallmentNumber() ==1 && !installments.isObligationsMet() && foreClosureDate.isBefore(installments.getDueDate())) ||
                 (Boolean.TRUE.equals(foreClosureDate.isAfter(installments.getDueDate()) &&  installments.isObligationsMet() && nextInstallment.getTotalPaid(currency).isZero()
                && Objects.isNull(nextInstallment.getObligationsMetOnDate()) && !isAdvance))){
            return ForeclosureEnum.FORECLOSURE_ON_BEFORE_DUEDATE;}

        /*Normal foreclosure scenario: The foreclosure date is on  due date */

        else if ((installments.getInstallmentNumber() ==1 && !installments.isObligationsMet() && foreClosureDate.isEqual(installments.getDueDate())) ||
                 foreClosureDate.isEqual(installments.getDueDate()) &&  installments.getTotalPaid(currency).isZero() && previousInstallment.isObligationsMet()) {
            return ForeclosureEnum.FORECLOSURE_ON_DUEDATE;
        }
         else if (!installments.isObligationsMet()
                 && installments.getInterestPaid().doubleValue()==0 &&
                 installments.getPrincipalCompleted().doubleValue()==0 &&
                 !previousInstallment.isObligationsMet() &&  previousInstallment.getInterestPaid().doubleValue()==0 &&
                 previousInstallment.getPrincipalCompleted().doubleValue()==0) {
             return ForeclosureEnum.FORECLOSURE_2MONTH_OVERDUE;}

        else if(!installments.isObligationsMet()
                 && installments.getInterestPaid().doubleValue()==0 &&
                 installments.getPrincipalCompleted().doubleValue()==0
                 && foreClosureDate.isAfter(installments.getDueDate())){
            return ForeclosureEnum.FORECLOSURE_1MONTH_OVERDUE;
         }
        /* foreclosure date is before due date and short paid interest on due date */
        else if(foreClosureDate.isAfter(installments.getDueDate()) &&  installments.isObligationsMet()
                && !nextInstallment.getInterestOutstanding(currency).isZero()
                && nextInstallment.getPrincipalCompleted().doubleValue()==0
                && foreClosureDate.isBefore(nextInstallment.getDueDate()) && Boolean.TRUE.equals(!isAdvance)){
            return ForeclosureEnum.FORCLOSURE_INTEREST_SHORT_PAID_ON_BEFORE_DUEDATE;}

        /* foreclosure date is on due date and short paid interest on due date */

        else if (foreClosureDate.isEqual(installments.getDueDate())
                &&  !installments.isObligationsMet()
                && !installments.getInterestOutstanding(currency).isZero()
                && installments.getPrincipalCompleted().doubleValue()==0) {
            return ForeclosureEnum.FORCLOSURE_INTEREST_SHORT_PAID_ON_DUEDATE;}

        /* foreclosure date is after due date and short paid interest on due date*/

        else if (foreClosureDate.isAfter(installments.getDueDate()) &&  !installments.isObligationsMet() && !installments.getInterestOutstanding(currency).isZero()
                && installments.getPrincipalCompleted().doubleValue()==0) {
            return ForeclosureEnum.FORCLOSURE_INTEREST_SHORT_PAID_AFTER_DUEDATE;}

        else if (foreClosureDate.isAfter(installments.getDueDate()) &&  installments.isObligationsMet()
                 && (nextInstallment.getInterestOutstanding(currency).isZero())
                 && nextInstallment.getPrincipalCompleted().doubleValue()==0
                 && foreClosureDate.isBefore(nextInstallment.getDueDate()) && Boolean.TRUE.equals(!isAdvance)) {
            
            return ForeclosureEnum.FORECLOSURE_INTEREST_FULLY_PAID_BEFORE_DUE_DATE;

         } else if (foreClosureDate.isEqual(installments.getDueDate())
                 &&  !installments.isObligationsMet()
                 &&  installments.getInterestOutstanding(currency).isZero()
                 && installments.getPrincipalCompleted().doubleValue()==0) {
            return ForeclosureEnum.FORECLOSURE_INTEREST_FULLY_PAID_ON_DUE_DATE;
             
         } else if (foreClosureDate.isAfter(installments.getDueDate())
                 &&  !installments.isObligationsMet()
                 && installments.getInterestOutstanding(currency).isZero()
                 && installments.getPrincipalCompleted().doubleValue()==0) {

            return ForeclosureEnum.FORECLOSURE_INTEREST_FULLY_PAID_ON_AFTER_DUE_DATE;
             
         } else if(foreClosureDate.isAfter(installments.getDueDate()) &&  installments.isObligationsMet()
                 && (nextInstallment.getInterestOutstanding(currency).isZero()) && nextInstallment.getPrincipalCompleted().doubleValue()>0
                 && foreClosureDate.isBefore(nextInstallment.getDueDate()) && !nextInstallment.isObligationsMet() && Boolean.TRUE.equals(!isAdvance)){

            return ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_BEFORE_DUEDATE;
        } else if (foreClosureDate.isEqual(installments.getDueDate())
                 &&  !installments.isObligationsMet()
                 && installments.getInterestOutstanding(currency).isZero()
                 && !installments.getPrincipalOutstanding(currency).isZero() ||
                 (loan.getTransactionProcessingStrategy().isVerticalStrategy() && installments.getInterestOutstanding(currency).isGreaterThanZero())) {

            return ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_ON_DUEDATE;

         }

        else if(foreClosureDate.isAfter(installments.getDueDate())
                 &&  (!installments.isObligationsMet() || (loan.getTransactionProcessingStrategy().isVerticalStrategy() && installments.getInterestOutstanding(currency).isGreaterThanZero()))
                 && installments.getInterestOutstanding(currency).isZero() && !installments.getPrincipalOutstanding(currency).isZero()){

            return ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_AFTER_DUEDATE;
         }

        /* Backdated foreclosure date is before due date and EMI fully paid */

        else if(installments.isObligationsMet() && nextInstallment.isObligationsMet()
                && foreClosureDate.isAfter(installments.getDueDate())
                && !isAdvance){
            return ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_BEFORE_DUE_DATE;
        }
        /* Backdated foreclosure date is on due date and EMI fully paid */

        else if(installments.isObligationsMet() && foreClosureDate.isEqual(installments.getDueDate()) && !isAdvance){
            return ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_DUEDATE;
        }

        /* Backdated foreclosure date is after due date and EMI fully paid */

        else if(installments.isObligationsMet() && foreClosureDate.isAfter(installments.getDueDate()) && !isAdvance){
            return ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_AFTER_DUEDATE;
        }

        /* Backdated foreclosure date is before due date excess amount paid */
        else if(Boolean.TRUE.equals(foreClosureDate.isAfter(installments.getDueDate())
                &&  installments.isObligationsMet() &&  isAdvance && nextInstallment.isObligationsMet())){
            return ForeclosureEnum.FORECLOSURE_ADVANCE_BEFORE_DUEDATE;
        }

        /* foreclosure date os on due date advance amount paid */
        else if (foreClosureDate.isEqual(installments.getDueDate()) && Boolean.TRUE.equals(isAdvance)) {
            return ForeclosureEnum.FORECLOSURE_ADVANCE_ON_DUEDATE;
        }
        /* foreclosure date as on after due date advance amount paid*/
        else if (foreClosureDate.isAfter(installments.getDueDate())
                 && Boolean.TRUE.equals(isAdvance)) {
            return ForeclosureEnum.FORECLOSURE_ADVANCE_AFTER_DUEDATE;
        }
        return null;
    }

    default LoanRepaymentScheduleInstallment foreclosureBasedOnScenario(ForeclosureEnum foreclosureEnum, LocalDate foreclosureDate,
                                                                        LoanRepaymentScheduleInstallment currentInstallemnt,
                                                                        MonetaryCurrency currency, Loan loan, GstService gstServiceimpl, AppUser currentUser,LoanAccrualRepository loanAccrualRepository,boolean isToLoadForeclosureTemplate){

        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = new LoanRepaymentScheduleInstallment(loan);
        LoanRepaymentScheduleInstallment previousInstallment = currentInstallemnt.getInstallmentNumber() == 1 ? currentInstallemnt
                : loan.fetchRepaymentScheduleInstallment(currentInstallemnt.getInstallmentNumber() -1);
        LoanRepaymentScheduleInstallment nextInstallment =Boolean.TRUE.equals(currentInstallemnt.isLastInstallment(currentInstallemnt.getInstallmentNumber(),loan.getNumberOfRepayments())) ?
                currentInstallemnt :loan.fetchRepaymentScheduleInstallment(currentInstallemnt.getInstallmentNumber()+1);
        List<LoanRepaymentScheduleInstallment> listOfInstallment = ForeclosureUtils.retrieveListOfInstallments(loan,foreclosureDate);
        ForeclosureData foreclosureData = null;
        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        BigDecimal advanceAmount = loan.retrieveAdvanceAmount();
        ProductCollectionConfig productCollectionConfig = loan.getLoanProduct().getProductCollectionConfig();
        BigDecimal principalOutstandingForInterest =  BigDecimal.ZERO;
        BigDecimal principalOutstandingForCharge =  BigDecimal.ZERO;

        switch (foreclosureEnum){

            case FORECLOSURE_ON_BEFORE_DUEDATE :

                principalOutstandingForInterest = currentInstallemnt.getInstallmentNumber() ==1 && !currentInstallemnt.isObligationsMet()
                        ? loan.getLoanSummary().getTotalPrincipalOutstanding()

                        : ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureOtherThanDueDateInterest(),currentInstallemnt,loan,foreclosureEnum);


                int daysBetweenForeclosure = currentInstallemnt.getInstallmentNumber() ==1 && !currentInstallemnt.isObligationsMet() ?
                        Math.toIntExact(ChronoUnit.DAYS.between(currentInstallemnt.getFromDate(),foreclosureDate)):
                        ForeclosureUtils.tillDays(currentInstallemnt,foreclosureDate);

                double interestForCurrentPeriod =  ForeclosureUtils.calculateForeclosureInterest(loan.getLoanProductRelatedDetail().getDaysInYearType(),
                        principalOutstandingForInterest, loan.getLoanProductRelatedDetail().getAnnualNominalInterestRate(),
                        daysBetweenForeclosure, foreclosureDate);

                double selfInterest = ForeclosureUtils.selfInterest(interestForCurrentPeriod,loan);

                double partnerInterest =ForeclosureUtils.partnerInterest(interestForCurrentPeriod,selfInterest);
                BigDecimal interest =  BigDecimal.valueOf(interestForCurrentPeriod).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
                BigDecimal selfInterests= BigDecimal.valueOf(selfInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
                BigDecimal partnerInterests =BigDecimal.valueOf(partnerInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
                BigDecimal feeAmount = BigDecimal.ZERO;

                foreclosureData =  new ForeclosureData(interest,selfInterests,partnerInterests,feeAmount);

                loanRepaymentScheduleInstallment =  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount() ,foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureOtherThanDueDateCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;
            // For on due Date getting the previous fully paid installment\
            case FORECLOSURE_ON_DUEDATE:
                principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureOnDueDateInterest(),previousInstallment,loan,foreclosureEnum);
                foreclosureData = foreclosureDataValueOnDuedate(previousInstallment,foreclosureDate,loan,principalOutstandingForInterest);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(previousInstallment.getInstallmentNumber());


                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureOnDueDateCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;

            case FORCLOSURE_INTEREST_SHORT_PAID_ON_BEFORE_DUEDATE:

                currentInstallemnt =  loan.fetchRepaymentScheduleInstallment(currentInstallemnt.getInstallmentNumber()+1);
                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);

                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedShortPaidInterestCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);

                break;

            case FORCLOSURE_INTEREST_SHORT_PAID_ON_DUEDATE:
                // TODO
                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());


                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureShortPaidInterestCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);

                break;

            case FORCLOSURE_INTEREST_SHORT_PAID_AFTER_DUEDATE:

                principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureShortPaidInterest(),currentInstallemnt,loan,foreclosureEnum);
                foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,principalOutstandingForInterest);


                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureShortPaidInterestCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;

            case FORECLOSURE_INTEREST_FULLY_PAID_BEFORE_DUE_DATE:

                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);

                 loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedFullyPaidInterestCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                break;

            case FORECLOSURE_INTEREST_FULLY_PAID_ON_DUE_DATE:

                foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,principalOutstandingForInterest);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureOnDueDateCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;

            case FORECLOSURE_INTEREST_FULLY_PAID_ON_AFTER_DUE_DATE:

                foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,currentInstallemnt.getPrincipalOutstanding());
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureOnDueDateCharge(),currentInstallemnt.getPrincipalOutstanding(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;

            case FORCLOSURE_PRINCIPAL_SHORT_PAID_BEFORE_DUEDATE:

                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedShortPaidPrincipalCharge(),currentInstallemnt.getPrincipalOutstanding(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);

                break;
                case FORCLOSURE_PRINCIPAL_SHORT_PAID_ON_DUEDATE:
                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest(),foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                    loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());


                    principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosurePrincipalShortPaidCharge(),currentInstallemnt.getPrincipalOutstanding(currency).getAmount(),loan,foreclosureEnum);
                    this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                    break;
                case FORCLOSURE_PRINCIPAL_SHORT_PAID_AFTER_DUEDATE:
                    principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosurePrincipalShortPaidInterest(),currentInstallemnt,loan,foreclosureEnum);
                    foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,principalOutstandingForInterest);
                    loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency), loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                    loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                    principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosurePrincipalShortPaidCharge(),currentInstallemnt.getPrincipalOutstanding(),loan,foreclosureEnum);
                    this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                    break;
                case
                    FORECLOSURE_EMI_FULLY_PAID_ON_AFTER_DUEDATE:
                    principalOutstandingForInterest = ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureBackdatedFullyPaidEmiInterest(),currentInstallemnt,loan,foreclosureEnum);
                    foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,principalOutstandingForInterest);
                    loanRepaymentScheduleInstallment= getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                            loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount())
                            ,foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                            foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                            loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                    loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                    principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedFullyPaidEmiCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                    this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                    break;

            case FORECLOSURE_EMI_FULLY_PAID_ON_BEFORE_DUE_DATE, FORECLOSURE_EMI_FULLY_PAID_ON_DUEDATE:

                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(currentInstallemnt.getInterestOutstanding(currency).getAmount()),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedFullyPaidEmiCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;
            case FORECLOSURE_1MONTH_OVERDUE:
                LoanRepaymentScheduleInstallment installment = listOfInstallment.get(listOfInstallment.size()-1);
                principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureOneMonthOverdueInterest(),installment,loan,foreclosureEnum);

                foreclosureData = foreclosureDataValueOnDuedate(installment,foreclosureDate,loan,principalOutstandingForInterest);
                Money remainigInterestOutstanding=ForeclosureUtils.getRemainingInterestOutstanding(listOfInstallment,currency,foreclosureDate);
                Money remainigSelfInterestOutstanding=ForeclosureUtils.getRemainingSelfInterestOutstanding(listOfInstallment,currency);
                Money remainigPartnerInterestOutstanding=ForeclosureUtils.getRemainingPartnerInterestOutstanding(listOfInstallment,currency);

                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(remainigInterestOutstanding.getAmount()),foreclosureData.selfInterest().add(remainigSelfInterestOutstanding.getAmount()),
                        foreclosureData.partnerInterest().add(remainigPartnerInterestOutstanding.getAmount()),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(installment.getInstallmentNumber());

                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureOneMonthOverdueCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;
                case FORECLOSURE_2MONTH_OVERDUE:
                LoanRepaymentScheduleInstallment installments = listOfInstallment.get(listOfInstallment.size()-1);
                principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureTwoMonthsOverdueInterest(),installments,loan,foreclosureEnum);

                foreclosureData = foreclosureDataValueOnDuedate(installments,foreclosureDate,loan,principalOutstandingForInterest);
                Money remainigInterestOutstandings=ForeclosureUtils.getRemainingInterestOutstanding(listOfInstallment,currency,foreclosureDate);
                Money remainigSelfInterestOutstandings=ForeclosureUtils.getRemainingSelfInterestOutstanding(listOfInstallment,currency);
                Money remainigPartnerInterestOutstandings=ForeclosureUtils.getRemainingPartnerInterestOutstanding(listOfInstallment,currency);

                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount().add(remainigInterestOutstandings.getAmount()),foreclosureData.selfInterest().add(remainigSelfInterestOutstandings.getAmount()),
                        foreclosureData.partnerInterest().add(remainigPartnerInterestOutstandings.getAmount()),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);

                    loanRepaymentScheduleInstallment.setInstallmentNumber(installments.getInstallmentNumber());

                    principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureTwoMonthsOverdueCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);

                break;

                case FORECLOSURE_ADVANCE_BEFORE_DUEDATE :
                foreclosureData = foreclosureValueOnCurrentInstallment(nextInstallment,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),BigDecimal.ZERO,foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(nextInstallment.getInstallmentNumber());

                loanRepaymentScheduleInstallment.setAdvanceAmount(advanceAmount);
                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureBackdatedAdvanceCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;

                case FORECLOSURE_ADVANCE_ON_DUEDATE:
                foreclosureData = foreclosureValueOnCurrentInstallment(currentInstallemnt,loan);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(currentInstallemnt.getInstallmentNumber());

                loanRepaymentScheduleInstallment.setAdvanceAmount(advanceAmount);
                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureAdvanceOnDueDateCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);

                break;
            case FORECLOSURE_ADVANCE_AFTER_DUEDATE:
                LoanRepaymentScheduleInstallment listInstallments = listOfInstallment.get(listOfInstallment.size()-1);
                principalOutstandingForInterest =  ForeclosureUtils.getForeclosureInterestCalPos(productCollectionConfig.getForeclosureAdvanceAfterDueDateInterest(),listInstallments,loan,foreclosureEnum);
                foreclosureData = foreclosureDataValueOnDuedate(currentInstallemnt,foreclosureDate,loan,principalOutstandingForInterest);
                loanRepaymentScheduleInstallment=  getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(),loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                        loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(),foreclosureData.interestAmount(),foreclosureData.selfInterest().add(currentInstallemnt.getSelfInterestOutstanding(currency).getAmount()),
                        foreclosureData.partnerInterest(),loan.getLoanSummary().getTotalSelfOutstanding(currency),
                        loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate ,BigDecimal.ZERO,BigDecimal.ZERO);
                loanRepaymentScheduleInstallment.setInstallmentNumber(listInstallments.getInstallmentNumber());

                loanRepaymentScheduleInstallment.setAdvanceAmount(advanceAmount);
                principalOutstandingForCharge = ForeclosureUtils.getForeclosureChargeCalculationPos(productCollectionConfig.getForeclosureAdvanceAfterDueDateCharge(),loanRepaymentScheduleInstallment.getPrincipal(),loan,foreclosureEnum);
                this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(),principalOutstandingForCharge,loan,gstServiceimpl,foreclosureDate,currentUser,loanRepaymentScheduleInstallment);
                break;
            case FORECLOSURE_METHODS:
                return this.foreclosureBasedOnMethods(foreclosureDate,currency,loan,gstServiceimpl,currentUser,currentInstallemnt,loanAccrualRepository,isToLoadForeclosureTemplate);
            default:
                break;
        }

        return loanRepaymentScheduleInstallment;

    }

    default ForeclosureData foreclosureDataValueOnDuedate(LoanRepaymentScheduleInstallment installment,LocalDate foreclosureDate,Loan loan,BigDecimal principalOutstanding){

        int daysBetweenForeclosure =  ForeclosureUtils.tillDays(installment,foreclosureDate);

        LOG.debug("Number Of Day For Foreclosure Interest Calculation {}",daysBetweenForeclosure);

        double interestForCurrentPeriod = installment.getInstallmentNumber() ==1 && !installment.isObligationsMet() && installment.getDueDate().equals(foreclosureDate)
                ? installment.getInterestCharged().doubleValue()
                : ForeclosureUtils.calculateForeclosureInterest(loan.getLoanProductRelatedDetail().getDaysInYearType(),
                principalOutstanding, loan.getLoanProductRelatedDetail().getAnnualNominalInterestRate(),
                daysBetweenForeclosure, foreclosureDate);

        double selfInterest = ForeclosureUtils.selfInterest(interestForCurrentPeriod,loan);

        double partnerInterest =ForeclosureUtils.partnerInterest(interestForCurrentPeriod,selfInterest);
        BigDecimal interest =  BigDecimal.valueOf(interestForCurrentPeriod).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal selfInterests= BigDecimal.valueOf(selfInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal partnerInterests =BigDecimal.valueOf(partnerInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal feeAmount = BigDecimal.ZERO;

        return  new ForeclosureData(interest,selfInterests,partnerInterests,feeAmount);
    }
    private static  LoanRepaymentScheduleInstallment  getForeclosureInstallment(BigDecimal totalPrincipal, BigDecimal selfPrincipal,
                                                                                BigDecimal partnerPrincipal,BigDecimal totalInterest ,BigDecimal selfInterestCharged,BigDecimal partnerInterestCharged,Money selfDue,
                                                                                Money partnerDue,LocalDate currentDate ,BigDecimal feeChargePortion,BigDecimal penaltyCharge){


        return new LoanRepaymentScheduleInstallment(null,0,currentDate,currentDate,totalPrincipal,totalInterest,
                feeChargePortion,penaltyCharge,false,null,null,selfPrincipal,
                partnerPrincipal,selfInterestCharged,partnerInterestCharged,selfDue.getAmount(),partnerDue.getAmount(),null,null);
    }


    default ForeclosureData foreclosureValueOnCurrentInstallment(LoanRepaymentScheduleInstallment installment,Loan loan){

        double interestForCurrentPeriod = installment.getInterestOutstanding(loan.getCurrency()).getAmount().doubleValue();

        double selfInterest = ForeclosureUtils.selfInterest(interestForCurrentPeriod,loan);

        double partnerInterest = ForeclosureUtils.partnerInterest(interestForCurrentPeriod,selfInterest);

        BigDecimal interest = BigDecimal.valueOf(interestForCurrentPeriod).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal selfInterests=   BigDecimal.valueOf(selfInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal partnerInterests = BigDecimal.valueOf(partnerInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal feeAmount = BigDecimal.ZERO;

        return  new ForeclosureData(interest,selfInterests,partnerInterests,feeAmount);
    }

    static  void modifyTransactionMappingForAdvanceTransaction(List<LoanRepaymentScheduleInstallment> modifiedInstallments, List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments,
                                                               List<LoanTransaction> loanTransactions, MonetaryCurrency currency,ForeclosureEnum foreclosureEnum){

        Long id = loanRepaymentScheduleInstallments.stream()
                .filter(repaymentScheduleInstallments -> !repaymentScheduleInstallments.isObligationsMet()
                        && (repaymentScheduleInstallments.getInterestOutstanding(currency).getAmount().doubleValue() > 0
                        || repaymentScheduleInstallments.getPrincipalOutstanding(currency).getAmount().doubleValue() > 0) && (foreclosureEnum.equals(ForeclosureEnum.FORCLOSURE_INTEREST_SHORT_PAID_ON_BEFORE_DUEDATE)
                        || foreclosureEnum.equals(ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_BEFORE_DUEDATE) || foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_INTEREST_FULLY_PAID_BEFORE_DUE_DATE)  || foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_ADVANCE_AFTER_DUEDATE) || foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_EMI_FULLY_PAID_ON_AFTER_DUEDATE) || foreclosureEnum.equals(ForeclosureEnum.FORECLOSURE_METHODS)
                        || foreclosureEnum.equals(ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_ON_DUEDATE)))
                .map(LoanRepaymentScheduleInstallment::getId).findFirst().orElse(null);

        loanTransactions.stream().forEach(loanTransaction -> {
            loanTransaction.getLoanTransactionToRepaymentScheduleMappings()
                    .stream()
                    .filter(loanTransactionToRepaymentScheduleMapping -> Objects.equals(loanTransactionToRepaymentScheduleMapping.getLoanRepaymentScheduleInstallment().getId(),id))
                    .forEach(loanTransactionToRepaymentScheduleMapping -> loanTransactionToRepaymentScheduleMapping.setInstallment(modifiedInstallments.get(modifiedInstallments.size()-1)));
        });
    }

    default void updateRepaymentSchedulePostForeclosure(Loan loan, LoanRepaymentScheduleHistoryRepository loanRepaymentScheduleHistoryRepository,
                                                        LoanRepaymentScheduleInstallment currentInstallment,LocalDate foreClosureDate,
                                                        BigDecimal principalOutstanding,BigDecimal interestOutstanding,BigDecimal selfPrincipalOutstanding,
                                                        ForeclosureEnum foreclosureEnum){



        /*Added For Backdated Foreclosure scenario To show the original Principal/ Interest Amount in Repayment schedule*/

        List<LoanRepaymentScheduleHistory> loanRepaymentScheduleHistories =  loanRepaymentScheduleHistoryRepository.getByLoanId(loan.getId());
        LoanRepaymentScheduleHistory repaymentScheduleHistory = loanRepaymentScheduleHistories.stream().filter(loanRepaymentScheduleHistory ->
                        Objects.equals(loanRepaymentScheduleHistory.getInstallmentNumber(),currentInstallment.getInstallmentNumber())).findFirst().orElseThrow();

        if(foreclosureEnum.equals(ForeclosureEnum.FORCLOSURE_PRINCIPAL_SHORT_PAID_ON_DUEDATE) && loan.getTransactionProcessingStrategy().isVerticalStrategy()){
            LoanRepaymentScheduleInstallment repaymentScheduleInstallment =
                    loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-1);
            repaymentScheduleInstallment.setPrincipalCompleted(repaymentScheduleInstallment.getPrincipalCompleted().add(repaymentScheduleHistory.getPrincipalCompleted()));
            repaymentScheduleInstallment.setSelfPrincipalCompleted(repaymentScheduleInstallment.getSelfPrincipalCompleted().add(repaymentScheduleHistory.getSelfPrincipalCompleted()));
            repaymentScheduleInstallment.setPartnerPrincipalCompleted(repaymentScheduleInstallment.getPartnerPrincipalCompleted().add(repaymentScheduleHistory.getPartnerPrincipalCompleted()));
            repaymentScheduleInstallment.setPrincipal(repaymentScheduleInstallment.getPrincipal(loan.getCurrency()).plus(repaymentScheduleHistory.getPrincipalCompleted()).getAmount());
            repaymentScheduleInstallment.setSelfPrincipal(repaymentScheduleInstallment.getSelfPrincipal(loan.getCurrency()).plus(repaymentScheduleHistory.getSelfPrincipalCompleted()).getAmount());
            repaymentScheduleInstallment.setPartnerPrincipal(repaymentScheduleInstallment.getPartnerPrincipal(loan.getCurrency()).plus(repaymentScheduleHistory.getPartnerPrincipalCompleted()).getAmount());


        }

        if(foreclosureEnum.notIn(foreclosureEnum) && repaymentScheduleHistory.isObligationsMet()
                ? loanRepaymentScheduleHistories.stream().filter(loanRepaymentScheduleHistory ->
                        Objects.equals(loanRepaymentScheduleHistory.getInstallmentNumber(),currentInstallment.getInstallmentNumber() + 1 ))
                .map(LoanRepaymentScheduleHistory::getInterestPaid).reduce(BigDecimal.ZERO,BigDecimal::add).doubleValue() > 0
                : repaymentScheduleHistory.getInterestPaid().doubleValue()>0
                &&  (foreClosureDate.isBefore(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()))
                || foreClosureDate.isEqual(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant())))){

            LoanRepaymentScheduleInstallment foreclosedInstallment =  loan.getRepaymentScheduleInstallments().stream()
                    .sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getInstallmentNumber)).reduce((first, secound)-> secound).orElse(null);

            foreclosedInstallment.setInterestCharged(!repaymentScheduleHistory.isObligationsMet() && repaymentScheduleHistory.getInterestPaid().doubleValue()>0
                    && foreClosureDate.isAfter(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant())) ?
                    interestOutstanding:loanRepaymentScheduleHistories.stream().filter(loanRepaymentScheduleHistory ->
                            Objects.equals(loanRepaymentScheduleHistory.getInstallmentNumber(),loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-1).getInstallmentNumber()))
                    .map(LoanRepaymentScheduleHistory::getInterestCharged).reduce(BigDecimal.ZERO,BigDecimal::add));

            foreclosedInstallment.setSelfInterestCharged(!repaymentScheduleHistory.isObligationsMet() && repaymentScheduleHistory.getInterestPaid().doubleValue()>0
                    && foreClosureDate.isAfter(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant())) ?
                    selfPrincipalOutstanding:loanRepaymentScheduleHistories.stream().filter(loanRepaymentScheduleHistory ->
                            Objects.equals(loanRepaymentScheduleHistory.getInstallmentNumber(),loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-1).getInstallmentNumber()))
                    .map(LoanRepaymentScheduleHistory::getInterestCharged).reduce(BigDecimal.ZERO,BigDecimal::add));

            foreclosedInstallment.setPrincipal((foreclosureEnum.isAfterDueDate(foreclosureEnum)
                    ? foreclosedInstallment.getPrincipalOutstanding(): principalOutstanding));

            foreclosedInstallment.setPrincipalCompleted((foreclosureEnum.isAfterDueDate(foreclosureEnum)
                    ? foreclosedInstallment.getPrincipalOutstanding(): principalOutstanding));

            foreclosedInstallment.setSelfPrincipal((foreclosureEnum.isAfterDueDate(foreclosureEnum)
                    ? foreclosedInstallment.getSelfPrincipalOutstanding(): selfPrincipalOutstanding));

            foreclosedInstallment.setSelfPrincipalCompleted(foreclosureEnum.isAfterDueDate(foreclosureEnum)
                    ? foreclosedInstallment.getSelfPrincipalOutstanding(): selfPrincipalOutstanding);

            foreclosedInstallment.setInterestPaid(foreclosedInstallment.getDueDate().isBefore(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()))
                    ||LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()).isEqual(foreclosedInstallment.getDueDate())
                    ? repaymentScheduleHistory.getInterestCharged(): foreclosedInstallment.getInterestCharged());

            foreclosedInstallment.setSelfInterestPaid(foreclosedInstallment.getDueDate().isBefore(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()))
                    ||LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()).isEqual(foreclosedInstallment.getDueDate())
                    ? repaymentScheduleHistory.getSelfInterestCharged(): foreclosedInstallment.getSelfInterestCharged());

            foreclosedInstallment.setPartnerInterestCharged(loan.getRepaymentScheduleInstallments().get(loan.getLoanRepaymentScheduleInstallmentsSize()-1).getDueDate().isBefore(LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()))
                    ||LocalDate.ofInstant(repaymentScheduleHistory.getDueDate().toInstant(), DateUtils.getDateTimeZoneOfTenant()).isEqual(foreclosedInstallment.getDueDate())
                    ? repaymentScheduleHistory.getPartnerInterestCharged(): foreclosedInstallment.getPartnerInterestCharged());

            loan.getLoanSummary().setTotalOutstanding(BigDecimal.ZERO);
        }
    }


    default void removingTheLoanTransactionForBackDateAdvance(LocalDate transactionDate,Loan loan,
                                                              LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment,List<LoanTransaction> listOfLoanTransaction){
        loan.getLoanTransactions()
                .stream()
                .filter(transaction -> (transactionDate.isEqual(loanRepaymentScheduleInstallment.getDueDate())
                        || transactionDate.isBefore(loanRepaymentScheduleInstallment.getDueDate()))
                        && transaction.getValueDate().after(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant())))
                .forEach(loantransacion->{
                    LoanTransaction newLoanTransaction = LoanTransaction.copyTransactionProperties(loantransacion);
                    LoanTransactionToRepaymentScheduleMapping mapping = LoanTransactionToRepaymentScheduleMapping.createFrom(newLoanTransaction,
                            loanRepaymentScheduleInstallment, newLoanTransaction.getPrincipalPortion(loan.getCurrency()),newLoanTransaction.getInterestPortion(loan.getCurrency()),
                            newLoanTransaction.getFeeChargesPortion(loan.getCurrency()), newLoanTransaction.getPenaltyChargesPortion(loan.getCurrency()),newLoanTransaction.getSelfPrincipalPortion(loan.getCurrency()),
                            newLoanTransaction.getPartnerPrincipalPortion(loan.getCurrency()),
                            newLoanTransaction.getSelfInterestPortion(loan.getCurrency()),newLoanTransaction.getPartnerInterestPortion(loan.getCurrency()),newLoanTransaction.getSelfDue(),newLoanTransaction.getPartnerDue(),newLoanTransaction.getAdvanceAmount());
                    newLoanTransaction.updateLoanTransactionToRepaymentScheduleMappings(mapping, newLoanTransaction);
                    newLoanTransaction.setValueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    loantransacion.setReversed();
                    listOfLoanTransaction.add(newLoanTransaction);

                });
    }

    default void updateForeclosureInstallment(LoanRepaymentScheduleInstallment foreclosureInstallment ,
                                              ForeclosureEnum foreclosureEnum,LocalDate transactionDate,Money  interestOutstanding,
                                              Money selfInterestOutstanding,Money  partnerInterestOutstanding,Money totalPrincipal,
                                              MonetaryCurrency currency,List<LoanRepaymentScheduleInstallment>  newInstallments,BigDecimal totalSelfPrincipal,Money totalPartnerPrincipal,Loan loan,LoanRepaymentScheduleInstallment installment,ProductCollectionConfig productCollectionConfig){

        LoanRepaymentScheduleInstallment nextInstallment = loan.fetchRepaymentScheduleInstallment(Boolean.TRUE.equals(installment.isLastInstallment(loan.getNumberOfRepayments(),installment.getInstallmentNumber()))
                ? installment.getInstallmentNumber(): installment.getInstallmentNumber() +1);

        LocalDate installmentStartDate = loan.getDisbursementDate();
        if (newInstallments.size() > 0) {
            installmentStartDate = newInstallments.get(newInstallments.size() - 1).getDueDate();
        }

        foreclosureInstallment.setPrincipal(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum)
                ? foreclosureInstallment.getPrincipal().add(nextInstallment.getPrincipalCompleted()) : totalPrincipal.getAmount() );
        foreclosureInstallment.setSelfPrincipal(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum)
                ? foreclosureInstallment.getSelfPrincipal(currency).add(nextInstallment.getSelfPrincipalCompleted()).getAmount() : totalSelfPrincipal );
        foreclosureInstallment.setPartnerPrincipal(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum)
                ? foreclosureInstallment.getPartnerPrincipal(currency).add(nextInstallment.getPartnerPrincipalCompleted()).getAmount() : totalPartnerPrincipal.getAmount());

        foreclosureInstallment.setPrincipalCompleted(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum) ? nextInstallment.getPrincipalCompleted() :BigDecimal.ZERO);
        foreclosureInstallment.setSelfPrincipalCompleted(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum) ? nextInstallment.getSelfPrincipalCompleted() :BigDecimal.ZERO);
        foreclosureInstallment.setPartnerPrincipalCompleted(foreclosureEnum.isBackdateEmiFullPaid(foreclosureEnum) ? nextInstallment.getPartnerPrincipalCompleted() :BigDecimal.ZERO);

        foreclosureInstallment.updateInterestCharged((foreclosureEnum.isAfterDueDate(foreclosureEnum) || ForeclosureUtils.isNeedToAppropriateTheInterest(productCollectionConfig.getForeclosureMethodType()))
                ? foreclosureInstallment.getInterestCharged(currency).getAmount().subtract(interestOutstanding.getAmount())
                :foreclosureInstallment.getInterestCharged());
        foreclosureInstallment.updateSelfInterestCharged((foreclosureEnum.isAfterDueDate(foreclosureEnum) || ForeclosureUtils.isNeedToAppropriateTheInterest(productCollectionConfig.getForeclosureMethodType()))
                ?foreclosureInstallment.getSelfInterestCharged(currency).getAmount().subtract(selfInterestOutstanding.getAmount())
                :foreclosureInstallment.getSelfInterestCharged());
        foreclosureInstallment.updatePartnerInterestCharged((foreclosureEnum.isAfterDueDate(foreclosureEnum) || ForeclosureUtils.isNeedToAppropriateTheInterest(productCollectionConfig.getForeclosureMethodType()))
                ?foreclosureInstallment.getPartnerFeeChargesCharged(currency).getAmount().subtract(partnerInterestOutstanding.getAmount())
                :foreclosureInstallment.getPartnerFeeChargesCharged());


        foreclosureInstallment.setFromDate(Date.from(installmentStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        foreclosureInstallment.setFromDate(Date.from(installmentStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        foreclosureInstallment.setDueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        foreclosureInstallment.setInstallmentNumber(newInstallments.size() + 1);

        foreclosureInstallment.updateAdvanceAmount(BigDecimal.ZERO);
        if(foreclosureEnum.isBackdateEmiFullPaidBeforeDueDate(foreclosureEnum)){
            newInstallments.get(newInstallments.size()-1).setPrincipal(foreclosureInstallment.getPrincipal(currency).add(nextInstallment.getPrincipal(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setSelfPrincipal(foreclosureInstallment.getSelfPrincipal(currency).add(nextInstallment.getSelfPrincipal(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setPartnerPrincipal(foreclosureInstallment.getPartnerPrincipal(currency).add(nextInstallment.getPartnerPrincipal(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setInterestCharged(foreclosureInstallment.getInterestCharged(currency).add(nextInstallment.getInterestCharged(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setSelfInterestCharged(foreclosureInstallment.getSelfInterestCharged(currency).add(nextInstallment.getSelfInterestCharged(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setPartnerInterestCharged(foreclosureInstallment.getPartnerInterestCharged(currency).add(nextInstallment.getPartnerInterestCharged(currency)).getAmount());
            newInstallments.get(newInstallments.size()-1).setPrincipalCompleted(nextInstallment.getPrincipalCompleted());
            newInstallments.get(newInstallments.size()-1).setSelfPrincipalCompleted( nextInstallment.getSelfPrincipalCompleted());
            newInstallments.get(newInstallments.size()-1).setPartnerPrincipalCompleted(nextInstallment.getPartnerPrincipalCompleted());
            newInstallments.get(newInstallments.size()-1).setInterestPaid(nextInstallment.getInterestPaid());
            newInstallments.get(newInstallments.size()-1).setSelfInterestPaid(nextInstallment.getInterestPaid());
            newInstallments.get(newInstallments.size()-1).setPartnerInterestPaid(nextInstallment.getInterestPaid());
            newInstallments.get(newInstallments.size()-1).setFeeChargesCharged(foreclosureInstallment.getFeeChargesCharged(currency).getAmount());
            newInstallments.get(newInstallments.size()-1).setSelfFeeChargesCharged(foreclosureInstallment.getSelfFeeChargesCharged(currency).getAmount());
            newInstallments.get(newInstallments.size()-1).setPartnerFeeChargesCharged(foreclosureInstallment.getPartnerFeeChargesCharged(currency).getAmount());
            newInstallments.get(newInstallments.size()-1).setObligationsMet(false);
            newInstallments.get(newInstallments.size()-1).setObligationsMetOnDate(null);
            newInstallments.get(newInstallments.size()-1).setFromDate(Date.from(installment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            newInstallments.get(newInstallments.size()-1).setDueDate(Date.from(transactionDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
    }

    default  void calculateForeclosureCharge(Set<LoanPenalForeclosureCharges> loanPenalForeclosureCharges,
                                             BigDecimal principalAmount, Loan loan, GstService gstServiceimpl,
                                             LocalDate foreclosureDate, AppUser currentUser,LoanRepaymentScheduleInstallment installment){

        loanPenalForeclosureCharges.stream()
                .filter( loanPenalForeclosureCharge-> loanPenalForeclosureCharge.getCharge().isForeclosureCharge()
                        && Objects.nonNull(loanPenalForeclosureCharge))
                .forEach(loanPenalForeclosureCharges1 ->  {

                    /*getting the charge that linked with loan*/
                    Charge foreclosureCharge = loanPenalForeclosureCharges1.getCharge();


                    /*calculating the foreclosureAmount*/
                    BigDecimal  foreClosureChargeAmount =foreclosureCharge.isFlatAmount() ? loanPenalForeclosureCharges1.getAmountOrPercentage()
                            : GstServiceImpl.percentage(principalAmount,loanPenalForeclosureCharges1.getAmountOrPercentage(),
                            foreclosureCharge.getChargeDecimal(),foreclosureCharge.getChargeRoundingMode());

                    /* calculating the gst */

                    List<Charge> charges = new ArrayList<>();
                    charges.add(foreclosureCharge);
                    List<GstData> gstData =  gstServiceimpl.calculationOfGst(loan.getClientId(),charges,principalAmount,loan.getLoanProduct(),null,loanPenalForeclosureCharges);

                    /* creating a new Loancharge for foreclosurecharge */

                    LoanCharge foreClosureloanCharge = new LoanCharge( loan,foreclosureCharge,principalAmount,loanPenalForeclosureCharges1.getAmountOrPercentage(),
                            ChargeTimeType.fromInt(foreclosureCharge.getChargeTimeType()), ChargeCalculationType.fromInt(foreclosureCharge.getChargeCalculation()),foreclosureDate, ChargePaymentMode.fromInt(foreclosureCharge.getChargePaymentMode()),
                            loan.getNumberOfRepayments(),BigDecimal.ZERO,loanPenalForeclosureCharges1.getSelfSharePercentage(),loanPenalForeclosureCharges1.getPartnerSharePercentage(),gstData, Integer.valueOf(0));
                    foreClosureloanCharge.setInstallmentNumber(loan.getRepaymentScheduleInstallments().get(loan.getRepaymentScheduleInstallments().size()-1).getInstallmentNumber());
                    foreClosureloanCharge.markAsFullyPaid();

                    /* foreclosure installment FeesAmount Adding */
                    installment.setFeeChargesCharged (GstEnum.isInclusive(GstEnum.fromInt(foreClosureloanCharge.getCharge().getGst()))
                            ? (foreClosureChargeAmount) : foreClosureChargeAmount.add(foreClosureloanCharge.getTotalGst()));
                    installment.setDue(ForeclosureUtils.total(installment.getDue(),foreClosureChargeAmount));

                    /* updating gst column in Loancharge Table */

                    List<LoanCharge> loanCharges = new ArrayList<>();
                    loanCharges.add(foreClosureloanCharge);
                    loan.updateGstForSelfAndPartnerInLoan(loanCharges,currentUser,true);

                    loan.addForclosureCharge(foreClosureloanCharge);

                    loan.getLoanSummary().updateTotalOutstanding(foreClosureChargeAmount,loan.getCurrency());

                    /* servicer fee calculation for foreclosure charge */
                    if(loan.getLoanProduct().getServicerFeeConfig() != null){
                        ServicerFeeAmountFormulaCalculation.calculateServicerFeeForCharge(loan,loanCharges,loan.getDisbursedAmount());}
                });
    }
    default  ForeclosureTemplateData retrieveForeclosureTemplateData(LoanRepaymentScheduleInstallment foreCloseDetail,MonetaryCurrency currency){

        return new ForeclosureTemplateData(foreCloseDetail.getInterestCharged(currency),foreCloseDetail.getFeeChargesCharged(currency),
                foreCloseDetail.getPenaltyChargesCharged(currency),foreCloseDetail.getPrincipal(currency),foreCloseDetail.getSelfPrincipal(currency),
                foreCloseDetail.getPartnerPrincipal(currency),foreCloseDetail.getSelfInterestCharged(currency),foreCloseDetail.getPartnerInterestCharged(currency),
                foreCloseDetail.getAdvanceAmount());
    }


    default Money appropriationLogicForForeclosureWithAdvanceAmount(Money transactionAmountRemaining, LoanTransaction loanTransaction,
                                                                    LoanRepaymentScheduleInstallment currentInstallment, LocalDate transactionDate,
                                                                    Loan loan, Money feeChargesPortion, LoanHistoryRepo loanHistoryRepo,
                                                                    LoanProduct loanProduct, MonetaryCurrency currency) {




        Money transactionAmountUnProcessed = transactionAmountRemaining;

        PrincipalAppropriationData principalAppropriationData = CollectionAppropriation.appropriatePrincipal(currentInstallment,transactionDate,
                transactionAmountUnProcessed);

        transactionAmountUnProcessed =transactionAmountUnProcessed.minus(principalAppropriationData.principal());

        Money principalPortion = principalAppropriationData.principal();
        Money selfPrincipalPortion = principalAppropriationData.selfPrincipal();
        Money partnerPrincipalPortion = principalAppropriationData.partnerPrincipal();

        Money interestPortion = loanTransaction.getInterestPortion(currency);
        Money selfInterestPortion = loanTransaction.getSelfInterestPortion(currency);
        Money partnerInterestPortion = loanTransaction.getPartnerInterestPortion(currency);

        Money penaltyChargesPortion = Money.zero(currency);

        Money amount = loanTransaction.getInterestPortion(currency).plus(loanTransaction.getPrincipalPortion(currency));
        Money selfDue = Money.zero(currency);
        Money partnerDue = Money.zero(currency);

        Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = loanTransaction.getLoanTransactionToRepaymentScheduleMappings();
        Iterator<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappingIterator = loanTransactionToRepaymentScheduleMappings.iterator();
        while (loanTransactionToRepaymentScheduleMappingIterator.hasNext()) {
            LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = loanTransactionToRepaymentScheduleMappingIterator.next();
            if (loanTransactionToRepaymentScheduleMapping.getAdvanceAmount().doubleValue() > 0) {
                loanTransactionToRepaymentScheduleMappingIterator.remove();
            }
        }

        LoanTransaction advanceTransaction = LoanTransaction.getNewLoanTransaction(loanTransaction, amount.getAmount(), principalPortion.getAmount(),
                interestPortion.getAmount(), feeChargesPortion.getAmount(), penaltyChargesPortion.getAmount(),
                selfPrincipalPortion.getAmount(), partnerPrincipalPortion.getAmount(), selfInterestPortion.getAmount(), partnerInterestPortion.getAmount(),
                selfDue.getAmount(), partnerDue.getAmount(), BusinessEventNotificationConstants.BusinessEvents.LOAN_FORECLOSURE.getValue());


        advanceTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        loan.addLoanTransaction(advanceTransaction);

        loanTransaction.setValueDate(loanTransaction.getTransactionDate().isEqual(currentInstallment.getDueDate()) || loanTransaction.getTransactionDate().isBefore(currentInstallment.getDueDate()) ?
                Date.from(currentInstallment.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()) :
                Date.from(loanTransaction.getTransactionDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));


        LoanTransactionToRepaymentScheduleMapping loanTransactionToRepaymentScheduleMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(loanTransaction, currentInstallment,
                principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, selfPrincipalPortion,
                partnerPrincipalPortion, selfInterestPortion, partnerInterestPortion, selfPrincipalPortion.add(selfInterestPortion).getAmount(),
                partnerPrincipalPortion.add(partnerInterestPortion).getAmount(), BigDecimal.ZERO);

        loanTransactionToRepaymentScheduleMapping.setAmount(BigDecimal.ZERO);

        advanceTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMapping, advanceTransaction);

        CollectionAppropriation.updateLoanHistory(loanTransaction, loanHistoryRepo, currentInstallment, loanTransaction.getLoan(), transactionDate);

        return transactionAmountUnProcessed;
    }
    default ForeclosureData getForeclosureInterestBasedOnMethod(ProductCollectionConfig productCollectionConfig,Loan loan,LocalDate foreclosureDate,LoanAccrualRepository loanAccrualRepository,boolean isToLoadForeclosureTemplate){
        double interestForCurrentPeriod = ForeclosureUtils.getInterestAmountBasedOnForeclosureType(productCollectionConfig.getForeclosureMethodType(),loan,foreclosureDate,loanAccrualRepository,isToLoadForeclosureTemplate).doubleValue();

        double selfInterest = ForeclosureUtils.selfInterest(interestForCurrentPeriod,loan);

        double partnerInterest =ForeclosureUtils.partnerInterest(interestForCurrentPeriod,selfInterest);
        BigDecimal interest =  BigDecimal.valueOf(interestForCurrentPeriod).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal selfInterests= BigDecimal.valueOf(selfInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal partnerInterests =BigDecimal.valueOf(partnerInterest).setScale(loan.getLoanProduct().getInterestDecimal(), loan.getLoanProduct().getInterestRoundingMode());
        BigDecimal feeAmount = BigDecimal.ZERO;

        return  new ForeclosureData(interest,selfInterests,partnerInterests,feeAmount);
    }

    default LoanRepaymentScheduleInstallment foreclosureBasedOnMethods(LocalDate foreclosureDate,
                                                                   MonetaryCurrency currency, Loan loan, GstService gstServiceimpl, AppUser currentUser,LoanRepaymentScheduleInstallment currentInstallment,LoanAccrualRepository loanAccrualRepository,boolean isToLoadForeclosureTemplate) {
        final LocalDate currentDate = DateUtils.getLocalDateOfTenant();
        ProductCollectionConfig productCollectionConfig = loan.getLoanProduct().getProductCollectionConfig();
        BigDecimal principalOutstanding = ForeclosureUtils.getForeclosurePrincipalBasedOnMethod(productCollectionConfig.getForeclosureMethodType(),currentInstallment ,loan);
        ForeclosureData foreclosureData = getForeclosureInterestBasedOnMethod(productCollectionConfig, loan,foreclosureDate,loanAccrualRepository,isToLoadForeclosureTemplate);
        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = getForeclosureInstallment(loan.getLoanSummary().getTotalPrincipalOutstanding(), loan.getLoanSummary().getTotalSelfPrincipalOutstanding(),
                loan.getLoanSummary().getTotalPartnerPrincipalOutstanding(), foreclosureData.interestAmount(), foreclosureData.selfInterest(), foreclosureData.partnerInterest(), loan.getLoanSummary().getTotalSelfOutstanding(currency),
                loan.getLoanSummary().getTotalPartnerOutstanding(currency), currentDate, BigDecimal.ZERO, BigDecimal.ZERO);
        this.calculateForeclosureCharge(loan.getLoanPenalForeclosueCharges(), principalOutstanding, loan, gstServiceimpl, foreclosureDate, currentUser, loanRepaymentScheduleInstallment);
        loanRepaymentScheduleInstallment.setAdvanceAmount(loan.retrieveAdvanceAmount());
        return loanRepaymentScheduleInstallment;
    }
}
