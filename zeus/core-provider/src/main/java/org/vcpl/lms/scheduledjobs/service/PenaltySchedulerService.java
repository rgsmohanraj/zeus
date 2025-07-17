package org.vcpl.lms.scheduledjobs.service;

import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.scheduledjobs.data.OverdueLoan;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.util.List;

public interface PenaltySchedulerService {

    List<OverdueLoan> retrieveOverdueLoans();
    void process();
    void applyPenalty(final OverdueLoan overdueLoan, final Loan loan, final AppUser systemUser);
}
