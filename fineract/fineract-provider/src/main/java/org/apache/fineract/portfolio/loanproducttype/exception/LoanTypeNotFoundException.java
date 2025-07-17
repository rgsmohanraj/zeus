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
package org.apache.fineract.portfolio.loanproducttype.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A {@link RuntimeException} thrown when type resources are not found.
 */

public class LoanTypeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoanTypeNotFoundException(final Long id) {
        super("error.msg.loanproducttype.id.invalid", "Type with identifier " + id + " does not exist", id);
    }

    public LoanTypeNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.loanproducttype.id.invalid", "Type with identifier " + id + " does not exist", id, e);
    }
}
