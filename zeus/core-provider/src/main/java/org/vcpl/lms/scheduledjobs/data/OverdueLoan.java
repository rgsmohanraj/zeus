package org.vcpl.lms.scheduledjobs.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanSchedulerRegistry;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueLoan {
    private Long loanId;
    private Long chargeId;
    private BigDecimal amount;
    private BigDecimal selfShare;
    private BigDecimal partnerShare;
    private Map<Integer, LoanSchedulerRegistry> overdueInstallments;

    @Override
    public boolean equals(Object loan) {
        return (this.loanId.equals(OverdueLoan.class.cast(loan).getLoanId()));
    }

    @Override
    public int hashCode() {
        return this.loanId.intValue();
    }

    public void addOverdueInstallments(Integer loanId, LoanSchedulerRegistry registry) {
        if(Objects.isNull(this.overdueInstallments)) {
            this.setOverdueInstallments(new HashMap<>());
        }
        this.overdueInstallments.put(loanId,registry);
    }
}
