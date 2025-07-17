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
package org.vcpl.lms.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.vcpl.lms.portfolio.collection.data.InterestAppropriationData;
import org.vcpl.lms.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.vcpl.lms.portfolio.loanaccount.data.MonthlyAccrualLoanDTO;
import org.vcpl.lms.portfolio.loanaccount.data.MonthlyAccrualLoanRecord;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;

public interface LoanAccrualWritePlatformService {

    void addAccrualAccounting(Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas) throws Exception;

    void addPeriodicAccruals(LocalDate tilldate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws Exception;

    void addIncomeAndAccrualTransactions(Long loanId) throws Exception;

    void updateMonthlyAccrualForCurrentMonthDisbursedLoans(List<MonthlyAccrualLoanDTO> currentMonthDisbursedLoans);

    void updateMonthlyAccrualForPreviousMonthDisbursedLoans(List<MonthlyAccrualLoanDTO> previousMonthDisbursedLoans);

    void updateMonthlyAccrualForCurrentMonthDueLoans(List<MonthlyAccrualLoanDTO> previousMonthDisbursedLoans);
    void reverseOnRepayment(Long loanId, LoanRepaymentScheduleInstallment installment, LocalDate transactionDate,
                            InterestAppropriationData interestData);
    void reverseOnForeclosure(Loan loan, Date transactionDate);
}
