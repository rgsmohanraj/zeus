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
package org.vcpl.lms.portfolio.loanproduct.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.vcpl.lms.portfolio.charge.domain.Charge;

import java.util.List;

public interface LoanProductFeesChargesRepository extends JpaRepository<LoanProductFeesCharges, Long>, JpaSpecificationExecutor<LoanProductFeesCharges> {

    LoanProductFeesCharges getByChargeAndLoanProduct(Charge charge, LoanProduct loanProduct);

    @Query("SELECT loanProductFeesCharges.charge.name FROM LoanProductFeesCharges loanProductFeesCharges " +
            " WHERE loanProductFeesCharges.charge.chargeTimeType != 18 " +
            " AND loanProductFeesCharges.loanProduct.id = :productId " +
            " ORDER BY loanProductFeesCharges.charge.id ASC")
    List<String> getChargesByLoanProductId(@Param("productId") Long productId);

    @Query("SELECT loanProductFeesCharges.charge.name FROM LoanProductFeesCharges loanProductFeesCharges " +
            " WHERE loanProductFeesCharges.charge.chargeTimeType NOT IN (1,17) " +
            " AND loanProductFeesCharges.loanProduct.id = :productId " +
            " ORDER BY loanProductFeesCharges.charge.id ASC")
    List<String> getChargesForCollection(@Param("productId") Long productId);

    @Query(" FROM LoanProductFeesCharges loanProductFeesCharges " +
            " WHERE loanProductFeesCharges.charge.chargeTimeType != 18 " +
            " AND loanProductFeesCharges.loanProduct.id = :productId AND loanProductFeesCharges.charge.isDefaultLoanCharge=:isDefaultLoanCharge " +
            " ORDER BY loanProductFeesCharges.charge.id ASC")
    List<LoanProductFeesCharges> getListChargesByLoanProductId(@Param("productId") Long productId,@Param("isDefaultLoanCharge") Boolean isDefaultLoanCharge);
}
