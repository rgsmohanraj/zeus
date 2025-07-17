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
package org.vcpl.lms.portfolio.loanaccount.servicerfee.service;

import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.monetary.domain.MonetaryCurrency;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanTransaction;
import org.vcpl.lms.portfolio.loanaccount.servicerfee.domain.ServicerFeeAmountCalculation;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;

import java.math.BigDecimal;
import java.util.List;

sealed public interface ServicerFeeWritePlatformService permits ServicerFeeWritePlatformServiceImp {

    CommandProcessingResult createServicerFeeConfiguration(JsonCommand command);

    CommandProcessingResult updateServicerFeeConfiguration(JsonCommand command, Long servicerFeeConfigId);



    static void calculateServicerFee(List<LoanTransaction> transactions, LoanProduct loanProduct, MonetaryCurrency currency, Loan loan){

        transactions.stream().filter(loanTransactions ->loanTransactions.isNotReversed() && !(loanTransactions.isDisbursement() || loanTransactions.isNonMonetaryTransaction()) && loanTransactions.getInterestPortion()!=null).forEach(loanTransactions-> {
            loanTransactions.getLoanTransactionToRepaymentScheduleMappings().stream().filter(loanTransactionToRepaymentScheduleMapping ->loanTransactionToRepaymentScheduleMapping.getInterestPortion(currency).getAmount().doubleValue()>0).forEach(loanTransactionToRepaymentScheduleMapping -> {
                if(loanProduct.isServicerFeeInterestConfigEnabled()) {
                    // step 1 calculating  VclInterestPortion
                    final BigDecimal VclInterestPortion = ServicerFeeAmountFormulaCalculation.vclInterestAmt(loanTransactionToRepaymentScheduleMapping.getSelfInterestPortion(), loanProduct.getServicerFeeConfig().getVclHurdleRate(),
                            loan.getLoanProductRelatedDetail().getAnnualNominalInterestRate(), loanProduct.getServicerFeeConfig().getVclInterestDecimal(), loanProduct.getServicerFeeConfig().getVclInterestRound());
                    // step 2 calculating  sfInterestBaseAmount
                    final BigDecimal sfInterestBaseAmount = ServicerFeeAmountFormulaCalculation.sfInterestBaseAmount(loanTransactionToRepaymentScheduleMapping.getSelfInterestPortion(), VclInterestPortion);
                    // step 3 calculating  sfInterestGstLossAmount  if  SfBaseAmtGstLossEnabled

                    final BigDecimal sfInterestGstLossAmount = ServicerFeeAmountFormulaCalculation.sfInterestGstLossAmount(sfInterestBaseAmount, loanProduct.getServicerFeeConfig().getSfBaseAmtGstLoss(), loanProduct.getServicerFeeConfig().getVclInterestDecimal(), loanProduct.getServicerFeeConfig().getVclInterestRound());

                    //  step 4 calculating  sfInterestGstAmount based on  SfBaseAmtGstLossEnabled
                    final BigDecimal sfInterestGstAmount = ServicerFeeAmountFormulaCalculation.sfInterestGstAmount(loanProduct.getServicerFeeConfig().isSfBaseAmtGstLossEnabled() ? sfInterestGstLossAmount : sfInterestBaseAmount, loanProduct.getServicerFeeConfig().getSfGst(), loanProduct.getServicerFeeConfig().getSfGstDecimal(), loanProduct.getServicerFeeConfig().getSfGstRound());

                    //  step 5 calculating  sfInterestInvoiceAmount
                    final BigDecimal sfInterestInvoiceAmount = ServicerFeeAmountFormulaCalculation.sfInterestInvoiceAmount(loanProduct.getServicerFeeConfig().isSfBaseAmtGstLossEnabled() ? sfInterestGstLossAmount : sfInterestBaseAmount, sfInterestGstAmount, loanProduct.getServicerFeeConfig().getServicerFeeDecimal(), loanProduct.getServicerFeeConfig().getServicerFeeRound());

                    ServicerFeeAmountCalculation servicerFeeAmountCalculation = new ServicerFeeAmountCalculation();
                    servicerFeeAmountCalculation.setLoan(loan);
                    servicerFeeAmountCalculation.setLoanTransactionToRepaymentScheduleMapping(loanTransactionToRepaymentScheduleMapping);
                    servicerFeeAmountCalculation.setVclInterestAmtHurdleRate(VclInterestPortion);
                    servicerFeeAmountCalculation.setSfInterestBaseAmount(sfInterestBaseAmount);
                    // Assigning the  servicerFeeAmount value
                    servicerFeeAmountCalculation.setSfInterestGstLossAmount(loanProduct.getServicerFeeConfig().isSfBaseAmtGstLossEnabled() ? sfInterestGstLossAmount : BigDecimal.ZERO);
                    servicerFeeAmountCalculation.setSfInterestGstAmount(sfInterestGstAmount);
                    servicerFeeAmountCalculation.setSfInterestInvoiceAmount(sfInterestInvoiceAmount);
                    servicerFeeAmountCalculation.setDateOf(loanTransactions.getDateOf());
                    servicerFeeAmountCalculation.setCreatedDate(DateUtils.getDateOfTenant());
                    loanTransactionToRepaymentScheduleMapping.setServicerFeeAmountCalculation(servicerFeeAmountCalculation);
                }
            });
        });

    }
}
