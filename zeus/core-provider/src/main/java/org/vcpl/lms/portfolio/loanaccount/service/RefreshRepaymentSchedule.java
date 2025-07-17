package org.vcpl.lms.portfolio.loanaccount.service;


import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.calendar.data.CalendarHistoryDataWrapper;
import org.vcpl.lms.portfolio.calendar.domain.*;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

@Service
public final class RefreshRepaymentSchedule {
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final LoanUtilService loanUtilService;


    public RefreshRepaymentSchedule(CalendarInstanceRepository calendarInstanceRepository, LoanUtilService loanUtilService) {

        this.calendarInstanceRepository = calendarInstanceRepository;
        this.loanUtilService = loanUtilService;
    }


    public void updateLoan(Loan loan, LocalDate disbursementDate) {

        final CalendarInstance calendarInstance = this.calendarInstanceRepository.findCalendarInstaneByEntityId(loan.getId(),
                CalendarEntityType.LOANS.getValue());

        loan.getLoanProduct().validateDisbursementDate(disbursementDate,loan.getLoanProduct());
        Calendar calendar = null;
        CalendarHistoryDataWrapper calendarHistoryDataWrapper = null;

        if (calendarInstance != null) {
            calendar = calendarInstance.getCalendar();
            Set<CalendarHistory> calendarHistory = calendar.getCalendarHistory();
            calendarHistoryDataWrapper = new CalendarHistoryDataWrapper(calendarHistory);
        }
        calendar.setStartDate(Date.from(disbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        loan.setInterestChargedFromDate(Date.from(disbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        LocalDate firstRepaymentDate = loanUtilService.getCalculatedRepaymentsStartingFromDate(disbursementDate, loan, calendarInstance, calendarHistoryDataWrapper);
        loan.setExpectedFirstRepaymentOnDate(Date.from(firstRepaymentDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loan.setExpectedDisbursementDate(Date.from(disbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loan.setActualDisbursementDate(Date.from(disbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        loan.setSubmittedOnDate(Date.from(disbursementDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
}