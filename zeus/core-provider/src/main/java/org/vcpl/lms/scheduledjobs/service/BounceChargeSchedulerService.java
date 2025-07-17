package org.vcpl.lms.scheduledjobs.service;

import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.scheduledjobs.data.OverdueLoan;
import org.vcpl.lms.useradministration.domain.AppUser;

import java.util.List;

public interface BounceChargeSchedulerService {

    List<OverdueLoan> retrieveOverdueInstallmentsforBounceChargeCalc();
    void process();
    void applyBounceCharge(final OverdueLoan bounceLoan, final Loan loan, final AppUser systemUser);
}
