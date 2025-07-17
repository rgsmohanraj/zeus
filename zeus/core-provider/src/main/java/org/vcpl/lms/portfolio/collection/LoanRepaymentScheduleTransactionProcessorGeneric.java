package org.vcpl.lms.portfolio.collection;


import org.vcpl.lms.portfolio.collection.service.CollectionAppropriation;
import org.vcpl.lms.portfolio.collection.service.HorizontalInterestPrincipalAppropriation;
import org.vcpl.lms.portfolio.collection.service.HorizontalInterestPrincipalChargesAppropriation;
import org.vcpl.lms.portfolio.collection.service.VerticalInterestPrincipalAppropriation;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class LoanRepaymentScheduleTransactionProcessorGeneric {

    private LoanRepaymentScheduleTransactionProcessorGeneric() {
    }

    private static <T extends CollectionAppropriation> T createInstance(Class<T> targetClass) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Constructor<T> constructor = targetClass.getDeclaredConstructor();
        return constructor.newInstance();
    }

    public static CollectionAppropriation determineStrategy(final LoanTransactionProcessingStrategy transactionProcessingStrategy)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {

        CollectionAppropriation processor = new HorizontalInterestPrincipalChargesAppropriation();
        if (transactionProcessingStrategy != null) {
            if (transactionProcessingStrategy.isInterestPrincipalPenaltiesFeesOrderStrategy()) {
                processor = createInstance(HorizontalInterestPrincipalChargesAppropriation.class);
            }
            if (transactionProcessingStrategy.isVerticalStrategy()) {
                processor = createInstance(VerticalInterestPrincipalAppropriation.class);
            }
            if (transactionProcessingStrategy.isHorizontalInterestPrincipal()) {
                processor = createInstance(HorizontalInterestPrincipalAppropriation.class);
            }
        }

        return processor;
    }
}
