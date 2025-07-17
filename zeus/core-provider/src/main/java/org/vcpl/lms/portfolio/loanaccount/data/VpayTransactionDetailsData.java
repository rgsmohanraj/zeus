package org.vcpl.lms.portfolio.loanaccount.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.constant.VPayTransactionConstants;
import org.vcpl.lms.portfolio.loanaccount.domain.VPayTransactionDetails;

import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VpayTransactionDetailsData {
    private Long id;
    private Long loanId;
    private Long clientId;
    private String transactionType;
    private String eventType;
    private String vpayReferenceId;
    private String utr;
    private String action;
    private String reason;
    private BigDecimal transactionAmount;
    private Date transactionDate;
    private Long createdBy;
    private Long partnerId;
    public static VpayTransactionDetailsData pennyDropTransaction(final Long loanId, final Long clientId, final String transactionType,
                                                              final String vpayReferenceId, final BigDecimal amount, final Date date,
                                                                  final Long createdBy) {
        return new VpayTransactionDetailsData(0l,loanId,clientId,transactionType,"PENNY_DROP",vpayReferenceId,
                null,VPayTransactionConstants.Action.IN_PROGRESS,null,amount, date, createdBy,null);
    }

    public static VpayTransactionDetailsData disbursementTransaction(final Long loanId,final Long clientId,final String transactionType,
                                                                 final String vpayReferenceId, final Long createdBy) {
        VpayTransactionDetailsData vpayTransactionDetailsData = new VpayTransactionDetailsData();
        vpayTransactionDetailsData.setLoanId(loanId);
        vpayTransactionDetailsData.setClientId(clientId);
        vpayTransactionDetailsData.setAction(VPayTransactionConstants.Action.IN_PROGRESS);
        vpayTransactionDetailsData.setTransactionType(transactionType);
        vpayTransactionDetailsData.setEventType("DISBURSEMENT");
        vpayTransactionDetailsData.setVpayReferenceId(vpayReferenceId);
        vpayTransactionDetailsData.setCreatedBy(createdBy);
        return vpayTransactionDetailsData;
    }
}
