package org.vcpl.lms.portfolio.loanaccount.domain.xirr;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class XirrTransaction {
    final double amount;
    final LocalDate when;

    /**
     * @Author Doni Sharmila
     * Construct a Transaction instance with the given amount at the given day.
     * @param amount the amount transferred
     * @param when the day the transaction took place
     */
    public XirrTransaction(double amount, LocalDate when) {
        this.amount = amount;
        this.when = when;
    }

    public XirrTransaction(double amount, Date when) {
        this.amount = amount;
        this.when = LocalDate.from(when.toInstant().atZone(ZoneId.systemDefault()));
    }

    public XirrTransaction(double amount, String when) {
        this.amount = amount;
        this.when = LocalDate.parse(when);
    }

    /**
     * The amount transferred in this transaction.
     * @return amount transferred in this transaction
     */
    public double getAmount() {
        return amount;
    }

    /**
     * The day the transaction took place.
     * @return day the transaction took place
     */
    public LocalDate getWhen() {
        return when;
    }
}
