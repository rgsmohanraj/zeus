package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vcpl.lms.commands.domain.CommandWrapper;
import org.vcpl.lms.commands.service.CommandWrapperBuilder;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.portfolio.charge.domain.Charge;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.ChargeRepaymentRequest;
import org.vcpl.lms.portfolio.loanaccount.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeCollection;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeCollectionRequest;
import org.vcpl.lms.portfolio.loanaccount.domain.Loan;
import org.vcpl.lms.portfolio.loanaccount.domain.LoanRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.compare.ComparableUtils.is;

@Service
@RequiredArgsConstructor
public class ChargeCollectionServiceImpl implements ChargeCollectionService {
    private final LoanRepository loanRepository;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Override
    public List<BulkApiResponse> process(final ChargeCollectionRequest request) {
        return process(request.getChargeCollections());
    }

    @Override
    public List<BulkApiResponse> process(List<ChargeCollection> chargeCollections) {
        List<BulkApiResponse> bulkApiResponses = new ArrayList<>();
        chargeCollections.forEach(chargeCollection -> {
            BulkApiResponse response = null;
            try {
                response = new BulkApiResponse();
                Loan loan = loanRepository.getByAccountNumber(chargeCollection.getLoanAccountNo())
                        .orElseThrow(() -> {
                            final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.accountnumber.not.exist",
                                    "Loan Account Number does not exist - " + chargeCollection.getLoanAccountNo(),
                                    null, null);
                            return new PlatformApiDataValidationException(List.of(error));
                        });

                if(!loan.getExternalId().equals(chargeCollection.getExternalId())) {
                    final ApiParameterError error = ApiParameterError.parameterError("error.msg.loan.externalId.mismatch",
                            "Given External Id is not linked to Loan Id: " + chargeCollection.getLoanAccountNo(),
                            null, null);
                    throw new PlatformApiDataValidationException(List.of(error));
                }
                Map<String, Long> chargeMap = loan.getLoanProduct().getCharges().stream()
                        .collect(Collectors.toMap(Charge::getName, Charge::getId));
                ChargeRepaymentRequest chargeRepaymentRequest = new ChargeRepaymentRequest();
                chargeRepaymentRequest.setLoanAccountNo(chargeCollection.getLoanAccountNo());
                chargeRepaymentRequest.setExternalId(chargeCollection.getExternalId());
                response.setLoanAccountNo(chargeCollection.getLoanAccountNo());
                response.setExternalId(chargeCollection.getExternalId());
                chargeRepaymentRequest.setTransactionDate(chargeCollection.getTransactionDate());
                chargeRepaymentRequest.setPartnerTransferDate(chargeCollection.getPartnerTransferDate());
                chargeRepaymentRequest.setPartnerTransferUtr(chargeCollection.getPartnerTransferUtr());
                chargeRepaymentRequest.setRepaymentMode(chargeCollection.getRepaymentMode());
                chargeRepaymentRequest.setReceiptReferenceNumber(chargeCollection.getReceiptReferenceNumber());
                chargeRepaymentRequest.setLocale("en");
                chargeRepaymentRequest.setDateFormat("dd MMMM yyyy");
                chargeCollection.getCharges().forEach(charge -> {
                    if (Objects.nonNull(charge.getPaid()) && is(charge.getPaid()).greaterThan(BigDecimal.ZERO)) {
                        chargeRepaymentRequest.setAmount(charge.getPaid());
                        chargeRepaymentRequest.setChargeId(chargeMap.get(charge.getName()));
                        payCharge(chargeRepaymentRequest);
                    }
                    if (Objects.nonNull(charge.getWaivedOff()) && is(charge.getWaivedOff()).greaterThan(BigDecimal.ZERO)) {
                        chargeRepaymentRequest.setAmount(charge.getWaivedOff());
                        chargeRepaymentRequest.setChargeId(chargeMap.get(charge.getName()));
                        waiveCharge(chargeRepaymentRequest);
                    }
                });
                response.setStatus("Success");
            } catch (PlatformApiDataValidationException exception){
                response.setStatus("Failure");
                response.setReason(exception.getErrors().stream().map(ApiParameterError::getDeveloperMessage)
                        .collect(Collectors.joining(",")));
            } catch (Exception exception) {
                response.setStatus("Failure");
                response.setReason(exception.getMessage());
            }
            bulkApiResponses.add(response);
        });
        return bulkApiResponses;
    }
    private void waiveCharge(ChargeRepaymentRequest chargeRepaymentRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .waiveLoanCharge(chargeRepaymentRequest.getLoanId(), chargeRepaymentRequest.getChargeId())
                .withJson(new Gson().toJson(chargeRepaymentRequest))
                .build();
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
    private void payCharge(ChargeRepaymentRequest chargeRepaymentRequest) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder()
                .payLoanCharge(chargeRepaymentRequest.getLoanId(), chargeRepaymentRequest.getChargeId())
                .withJson(new Gson().toJson(chargeRepaymentRequest))
                .build();
        commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }
}
