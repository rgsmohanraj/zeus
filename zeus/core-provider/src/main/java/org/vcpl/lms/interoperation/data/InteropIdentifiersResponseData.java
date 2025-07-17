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
package org.vcpl.lms.interoperation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.NotNull;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.interoperation.domain.InteropIdentifier;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccount;

public class InteropIdentifiersResponseData extends CommandProcessingResult {

    @NotNull
    private List<InteropIdentifierData> identifiers;

    protected InteropIdentifiersResponseData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly,
            @NotNull List<InteropIdentifierData> identifiers) {
        super(resourceId, officeId, commandId, changesOnly);
        this.identifiers = identifiers;
    }

    protected InteropIdentifiersResponseData(@NotNull List<InteropIdentifierData> identifiers) {
        this(null, null, null, null, identifiers);
    }

    public static InteropIdentifiersResponseData build(SavingsAccount account) {
        List<InteropIdentifierData> result = new ArrayList<>();
        if (account != null) {
            for (InteropIdentifier identifier : account.getIdentifiers()) {
                result.add(InteropIdentifierData.build(identifier));
            }
        }
        return new InteropIdentifiersResponseData(result);
    }
}
