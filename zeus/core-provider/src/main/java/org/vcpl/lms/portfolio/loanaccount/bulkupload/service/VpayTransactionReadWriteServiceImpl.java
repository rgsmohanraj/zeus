package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.loanaccount.data.VpayTransactionDetailsData;

import java.sql.ResultSet;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VpayTransactionReadWriteServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(VpayTransactionReadWriteServiceImpl.class);
    @Autowired private JdbcTemplate jdbcTemplate;

    private final RowMapper<VpayTransactionDetailsData> VPayTransactionDetailsMapper = (final ResultSet rs, final int rownum) ->
            new VpayTransactionDetailsData(rs.getLong("id"), rs.getLong("loan_id"), rs.getLong("client_id"),
                    rs.getString("payment_type"),rs.getString("event_type"),
                    rs.getString("vpay_reference_id"),rs.getString("utr"),
                    rs.getString("action"),rs.getString("reason"),
                    rs.getBigDecimal("transaction_amount"), rs.getDate("transaction_datetime"),
                    rs.getLong("created_by"),null);

    private final RowMapper<VpayTransactionDetailsData> VPayTransactionDetailsMapperWithPartner = (final ResultSet rs, final int rownum) ->
            new VpayTransactionDetailsData(rs.getLong("id"), rs.getLong("loan_id"), rs.getLong("client_id"),
                    rs.getString("payment_type"),rs.getString("event_type"),
                    rs.getString("vpay_reference_id"),rs.getString("utr"),
                    rs.getString("action"),rs.getString("reason"),
                    rs.getBigDecimal("transaction_amount"), rs.getDate("transaction_datetime"),
                    rs.getLong("created_by"), rs.getLong("partner_id"));
    public Map<String, VpayTransactionDetailsData> getAllIncompleteTransactionForEventType(String eventType) {
        String query = """
                    SELECT id, loan_id, client_id, payment_type, event_type, vpay_reference_id, utr, action, 
                    reason, transaction_amount, transaction_datetime, created_by,
                    (SELECT product_id FROM m_loan where id = t.loan_id) as partner_id 
                    FROM m_vpay_transaction_details t 
                    WHERE t.event_type = ? AND t.utr IS NULL 
                    AND ( t.action IS NULL OR t.action = 'INPROGRESS' OR t.action = 'SUSPECT' OR t.action = 'PROCESSED') """;

            return jdbcTemplate.query(query, VPayTransactionDetailsMapperWithPartner, new Object[] {eventType})
                    .stream()
                    .collect(Collectors.toMap(VpayTransactionDetailsData::getVpayReferenceId, Function.identity()));
    }

    public List<VpayTransactionDetailsData> updateTransactionResponse(Collection<VpayTransactionDetailsData> vPayTransactionDetailsDatas) {
        List<VpayTransactionDetailsData> updatedTransactions = new ArrayList<>();
        vPayTransactionDetailsDatas.forEach(vpayTransactionDetailsData -> {
            int updateCount = updateTransactionResponse(vpayTransactionDetailsData);
            if(updateCount == 1) updatedTransactions.add(vpayTransactionDetailsData);
        });
        return updatedTransactions;
    }

    public int updateTransactionResponse(VpayTransactionDetailsData vPayTransactionDetailsData) {
        String updatePennyDropStatusQuery = """
                UPDATE m_vpay_transaction_details 
                SET utr = ?, action = ?, reason = ?, transaction_amount = ?, transaction_datetime = ?
                WHERE id = ? """;
        return this.jdbcTemplate.update(updatePennyDropStatusQuery, new Object[]{vPayTransactionDetailsData.getUtr(),
                vPayTransactionDetailsData.getAction(), vPayTransactionDetailsData.getReason(),
                vPayTransactionDetailsData.getTransactionAmount(), vPayTransactionDetailsData.getTransactionDate(),
                vPayTransactionDetailsData.getId()});
    }

    public boolean createTransactionAcknowledgement(VpayTransactionDetailsData vpayTransactionDetailsData) {
        String insertTransactionQuery = """
                INSERT INTO m_vpay_transaction_details (loan_id, client_id,payment_type,event_type,vpay_reference_id, action, reason, created_by)\s
                values (?,?,?,?,?,?,?,?)
                """;
            int insertCount = this.jdbcTemplate.update(insertTransactionQuery, new Object[]{vpayTransactionDetailsData.getLoanId(), vpayTransactionDetailsData.getClientId(),
                vpayTransactionDetailsData.getTransactionType(), vpayTransactionDetailsData.getEventType(),
                vpayTransactionDetailsData.getVpayReferenceId(),vpayTransactionDetailsData.getAction(), vpayTransactionDetailsData.getReason(), vpayTransactionDetailsData.getCreatedBy()});
            return insertCount == 1;
    }

    public VpayTransactionDetailsData getByLoanIdAndEventType(Long loanId, String eventType) {
        try {
            String query = """
                SELECT * FROM m_vpay_transaction_details WHERE loan_id = ? AND event_type = ?
                """;
            return jdbcTemplate.queryForObject(query, VPayTransactionDetailsMapper, new Object[] { loanId, eventType });
        } catch (Exception exception) {
            LOG.info("No Data Found For LoanId: {} AND EventType: {}", new Object[]{ loanId, eventType});
            return null;
        }

    }
}
