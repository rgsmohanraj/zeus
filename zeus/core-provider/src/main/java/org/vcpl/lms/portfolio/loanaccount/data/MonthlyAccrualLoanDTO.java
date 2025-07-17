package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAccrualLoanDTO {
    private Long id;
    private Integer installment;
    private LocalDate disbursementDate;
    private LocalDate dueDate;
    private LocalDate maturityDate;
    private  BigDecimal principalOutstanding;
    private BigDecimal annualNominalInterestRate;
    private Integer interestCalculatedInPeriodEnum;
    private Long daysInYearEnum;
    private BigDecimal selfInterestShare;
    private BigDecimal partnerInterestShare;
    private Long days;
    private Boolean isDisbursementMonth;
    private Boolean isIntermediateMonth;
    private Boolean isDueMonth;

    public Boolean isDisbursementMonth() {
        return isDisbursementMonth;
    }

    public Boolean isIntermediateMonth() {
        return isIntermediateMonth;
    }

    public Boolean isDueMonth() {
        return isDueMonth;
    }

    public void setDisbursementDate(LocalDate disbursementDate) {
        this.disbursementDate = disbursementDate;
        this.isDisbursementMonth = (this.disbursementDate.isAfter(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()))
                && this.disbursementDate.isBefore(LocalDate.parse(DateUtils.getCurrentMonthLastDate())));
    }

    public void checkIsIntermediateMonth() {
        Objects.requireNonNull(this.disbursementDate, "Disbusement Date should not be null to evaluate Intermediate Month");
        Objects.requireNonNull(this.dueDate, "Due Date should not be null to evaluate Intermediate Month");
        this.isIntermediateMonth = (disbursementDate.isBefore(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()))
                && dueDate.isAfter(LocalDate.parse(DateUtils.getCurrentMonthLastDate())));
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.isDueMonth = (dueDate.isAfter(LocalDate.parse(DateUtils.getCurrentMonthFirstDate()))
                && dueDate.isBefore(LocalDate.parse(DateUtils.getCurrentMonthLastDate())));
    }
}
