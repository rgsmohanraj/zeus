package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.Data;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanStatus;

@Data
public class LoanFilterData
{
    final long loanId;
    final String accountNo;
    final String externalId;
    final String name;

    final long principal;

    final LoanStatus statusId;

    final String disbursedate;

    final String pennyDropStatus;
    final String pennyDropUTR;
    final String pennyDropFailureReason;
    final String disbursementStatus;
    final String disbursementUTR;
    final String disbursementFailureReason;
}
