package org.vcpl.lms.portfolio.loanaccount.domain;

import org.vcpl.lms.portfolio.common.BusinessEventNotificationConstants;

import java.util.Comparator;

public class ComparatorBasedOnValueDate implements Comparator<LoanTransaction> {

    @Override
    public int compare(LoanTransaction o1, LoanTransaction o2) {

        int compareResult = 0;
        // checking the transaction date is same
        final int comparsion = o1.getTransactionDate().compareTo(o2.getTransactionDate());
        if (comparsion == 0) {
            // if both value date are same cheking the tranaction is advance or not
          if(o1.getValueDate().equals(o2.getValueDate())){
              // comapring the each backdated advance Loan Transaction
             if(o1.getEvent().equals(BusinessEventNotificationConstants.BusinessEvents.ADVANCE_AMOUNT.getValue())){
                 compareResult =2;
             }
          }else if (o1.getValueDate().after(o2.getValueDate())) {
                compareResult = 1;
            } else if(o1.getValueDate().before(o2.getValueDate())) {
                compareResult = -1;
            }
        } else {
            compareResult = comparsion;
        }

        return compareResult;
    }
}
