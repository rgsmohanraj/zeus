package org.vcpl.lms.portfolio.collection.service;

import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransactionType;

public sealed interface CollectionWritePlatformService permits CollectionWritePlatformServiceImpl {

    CommandProcessingResult makeLoanRepayment(LoanTransactionType repaymentTransactionType, Long loanId, JsonCommand command,
                                              boolean isRecoveryRepayment);
}
