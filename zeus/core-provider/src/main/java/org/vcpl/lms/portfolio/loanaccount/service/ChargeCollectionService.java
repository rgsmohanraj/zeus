package org.vcpl.lms.portfolio.loanaccount.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.data.DataValidatorBuilder;
import org.vcpl.lms.infrastructure.core.exception.InvalidJsonException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.portfolio.loanaccount.data.BulkApiResponse;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeCollection;
import org.vcpl.lms.portfolio.loanaccount.data.ChargeCollectionRequest;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public interface ChargeCollectionService {
    Set<String> apiParameters = Set.of("chargeCollections");
    List<BulkApiResponse> process(final ChargeCollectionRequest request);
    List<BulkApiResponse> process(List<ChargeCollection> chargeCollections);
    default void sanitize(final String request,final FromJsonHelper jsonHelper) {
        // checks id the request is empty
        if (StringUtils.isBlank(request)) throw new InvalidJsonException();
        // checking for unsupported parameters
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        jsonHelper.checkForUnsupportedParameters(typeOfMap, request, apiParameters);
    }

    default ChargeCollectionRequest parse(final String request) {
        final Type typeOfMap = new TypeToken<ChargeCollectionRequest>() {}.getType();
        return new Gson().fromJson(request,typeOfMap);
    }


}
