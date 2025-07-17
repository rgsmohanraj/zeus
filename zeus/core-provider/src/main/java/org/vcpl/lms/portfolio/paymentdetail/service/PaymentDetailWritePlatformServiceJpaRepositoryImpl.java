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
package org.vcpl.lms.portfolio.paymentdetail.service;

import java.util.Map;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.portfolio.paymentdetail.PaymentDetailConstants;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetail;
import org.vcpl.lms.portfolio.paymentdetail.domain.PaymentDetailRepository;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentType;
import org.vcpl.lms.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentDetailWritePlatformServiceJpaRepositoryImpl implements PaymentDetailWritePlatformService {

    private final PaymentDetailRepository paymentDetailRepository;
    // private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper;

    @Autowired
    public PaymentDetailWritePlatformServiceJpaRepositoryImpl(final PaymentDetailRepository paymentDetailRepository,
            final PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper) {
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentTyperepositoryWrapper = paymentTyperepositoryWrapper;
    }

    @Override
    public PaymentDetail createPaymentDetail(final JsonCommand command, final Map<String, Object> changes) {
        final Long paymentTypeId = command.longValueOfParameterNamed(PaymentDetailConstants.paymentTypeParamName);
        if (paymentTypeId == null) {
            return null;
        }

        final PaymentType paymentType = this.paymentTyperepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        final PaymentDetail paymentDetail = PaymentDetail.generatePaymentDetail(paymentType, command, changes);
        return paymentDetail;

    }

    @Override
    @Transactional
    public PaymentDetail persistPaymentDetail(final PaymentDetail paymentDetail) {
        return this.paymentDetailRepository.save(paymentDetail);
    }

    @Override
    @Transactional
    public PaymentDetail createAndPersistPaymentDetail(final JsonCommand command, final Map<String, Object> changes) {
        final PaymentDetail paymentDetail = createPaymentDetail(command, changes);
        if (paymentDetail != null) {
            return persistPaymentDetail(paymentDetail);
        }
        return paymentDetail;
    }
}
