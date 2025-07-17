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
package org.vcpl.lms.portfolio.partner.domain;

import org.vcpl.lms.portfolio.partner.exception.PartnerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link PartnerRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class PartnerRepositoryWrapper {

    private final PartnerRepository repository;

    @Autowired
    public PartnerRepositoryWrapper(final PartnerRepository repository) {
        this.repository = repository;
    }

    public Partner findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new PartnerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Partner findPartnerHierarchy(final Long id) {
        final Partner partner = this.repository.findById(id).orElseThrow(() -> new PartnerNotFoundException(id));
//        partner.loadLazyCollections();
        return partner;

    }

    public Partner save(final Partner entity) {
        return this.repository.save(entity);
    }

    public Partner saveAndFlush(final Partner entity) {
        return this.repository.saveAndFlush(entity);
    }

    public void delete(final Partner entity) {
        this.repository.delete(entity);
    }
}
