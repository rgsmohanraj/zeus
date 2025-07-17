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
package org.vcpl.lms.portfolio.shareaccounts.service;

import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccount;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccountAssembler;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccountDomainService;
import org.vcpl.lms.portfolio.savings.domain.SavingsAccountTransaction;
import org.vcpl.lms.portfolio.shareaccounts.domain.ShareAccountDividendDetails;
import org.vcpl.lms.portfolio.shareaccounts.domain.ShareAccountDividendRepository;
import org.vcpl.lms.portfolio.shareaccounts.domain.ShareAccountDividendStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareAccountSchedularServiceImpl implements ShareAccountSchedularService {

    private final ShareAccountDividendRepository shareAccountDividendRepository;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final SavingsAccountAssembler savingsAccountAssembler;

    @Autowired
    public ShareAccountSchedularServiceImpl(final ShareAccountDividendRepository shareAccountDividendRepository,
            final SavingsAccountDomainService savingsAccountDomainService, final SavingsAccountAssembler savingsAccountAssembler) {
        this.shareAccountDividendRepository = shareAccountDividendRepository;
        this.savingsAccountDomainService = savingsAccountDomainService;
        this.savingsAccountAssembler = savingsAccountAssembler;
    }

    @Override
    @Transactional
    public void postDividend(final Long dividendDetailId, final Long savingsId) {

        ShareAccountDividendDetails shareAccountDividendDetails = this.shareAccountDividendRepository.findById(dividendDetailId).get();
        final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsId, false);
        SavingsAccountTransaction savingsAccountTransaction = this.savingsAccountDomainService.handleDividendPayout(savingsAccount,
                DateUtils.getLocalDateOfTenant(), shareAccountDividendDetails.getAmount(), false);
        shareAccountDividendDetails.update(ShareAccountDividendStatusType.POSTED.getValue(), savingsAccountTransaction.getId());
        this.shareAccountDividendRepository.saveAndFlush(shareAccountDividendDetails);
    }

}
