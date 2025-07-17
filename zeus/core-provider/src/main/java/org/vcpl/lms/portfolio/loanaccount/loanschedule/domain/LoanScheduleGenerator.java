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
package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.portfolio.loanaccount.data.HolidayDetailDTO;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanCharge;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.vcpl.lms.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.vcpl.lms.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;

public interface LoanScheduleGenerator {

    LoanScheduleModel generate(MathContext mc, LoanApplicationTerms loanApplicationTerms, List<LoanCharge> loanCharges,
            HolidayDetailDTO holidayDetailDTO);

    LoanScheduleDTO rescheduleNextInstallments(MathContext mc, LoanApplicationTerms loanApplicationTerms, Loan loan,
            HolidayDetailDTO holidayDetailDTO, LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor,
            LocalDate rescheduleFrom);

    LoanRepaymentScheduleInstallment calculatePrepaymentAmount(MonetaryCurrency currency, LocalDate onDate,
            LoanApplicationTerms loanApplicationTerms, MathContext mc, Loan loan, HolidayDetailDTO holidayDetailDTO,
            LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor);

}
