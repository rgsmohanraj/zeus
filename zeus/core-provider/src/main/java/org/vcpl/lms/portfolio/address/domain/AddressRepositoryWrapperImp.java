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
package org.vcpl.lms.portfolio.address.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.portfolio.address.exception.AddressNotFoundException;

/**
 * <p>
 * Wrapper for {@link AddressRepository} that is responsible for checking if {@link Address} is returned when using
 * <code>findOne</code> repository method and throwing an appropriate not found exception.
 * </p>
 *
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code base where {@link AddressRepository} is
 * required.
 * </p>
 */
@Service
public class AddressRepositoryWrapperImp {

    private final AddressRepository addressRepository;

@Autowired
    public AddressRepositoryWrapperImp(AddressRepository addressRepository) {

        this.addressRepository = addressRepository;
    }

    public Address findOneWithNotFoundDetection(final Long id) {

        final Address address = this.addressRepository.findById(id).orElseThrow(() -> new AddressNotFoundException(id));

        return address;
    }
}
