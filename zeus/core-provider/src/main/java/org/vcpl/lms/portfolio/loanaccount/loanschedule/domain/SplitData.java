package org.vcpl.lms.portfolio.loanaccount.loanschedule.domain;

import org.vcpl.lms.organisation.monetary.domain.Money;


public class SplitData {

    private final Money selfPrincipal;
    private final Money partnerPrincipal;
    private final Money selfInterestCharged;
    private final Money partnerInterestCharged;
    private final Money selfDue;
    private final Money partnerDue;

    public SplitData(Money selfPrincipal, Money partnerPrincipal, Money selfInterestCharged, Money partnerInterestCharged, Money selfDue, Money partnerDue) {
        this.selfPrincipal = selfPrincipal;
        this.partnerPrincipal = partnerPrincipal;
        this.selfInterestCharged = selfInterestCharged;
        this.partnerInterestCharged = partnerInterestCharged;
        this.selfDue=selfDue;
        this.partnerDue=partnerDue;
    }

    public Money selfPrincipal() {
        return this.selfPrincipal;
    }

    public Money partnerPrincipal() {
        return this.partnerPrincipal;
    }

    public Money selfInterestCharged() {
        return this.selfInterestCharged;
    }

    public Money partnerInterestCharged() {
        return partnerInterestCharged;
    }

    public Money selfDue() {
        return selfDue;
    }

    public Money partnerDue() {
        return partnerDue;
    }


    public Money getSelfPrincipal() {
        return selfPrincipal;
    }

    public Money getPartnerPrincipal() {
        return partnerPrincipal;
    }

    public Money getSelfInterestCharged() {
        return selfInterestCharged;
    }

    public Money getPartnerInterestCharged() {
        return partnerInterestCharged;
    }

    public Money getSelfDue() {
        return selfDue;
    }

    public Money getPartnerDue() {
        return partnerDue;
    }



}
