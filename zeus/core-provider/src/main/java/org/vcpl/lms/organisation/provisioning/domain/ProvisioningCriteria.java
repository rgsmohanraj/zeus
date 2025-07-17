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
package org.vcpl.lms.organisation.provisioning.domain;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.vcpl.lms.accounting.glaccount.domain.GLAccount;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractAuditableCustom;
import org.vcpl.lms.organisation.provisioning.constants.ProvisioningCriteriaConstants;
import org.vcpl.lms.organisation.provisioning.data.ProvisioningCriteriaDefinitionData;
import org.vcpl.lms.portfolio.loanproduct.domain.LoanProduct;
import org.vcpl.lms.useradministration.domain.AppUser;

@Entity
@Table(name = "m_provisioning_criteria", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "criteria_name" }, name = "criteria_name") })
public class ProvisioningCriteria extends AbstractAuditableCustom {

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true, fetch = FetchType.EAGER)
    Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "criteria", orphanRemoval = true, fetch = FetchType.EAGER)
    Set<LoanProductProvisionCriteria> loanProductMapping = new HashSet<>();

    public String getCriteriaName() {
        return this.criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }

    protected ProvisioningCriteria() {

    }

    public ProvisioningCriteria(String criteriaName, AppUser createdBy, ZonedDateTime createdDate, AppUser lastModifiedBy,
            ZonedDateTime lastModifiedDate) {
        this.criteriaName = criteriaName;
        setCreatedBy(createdBy);
        setCreatedDate(Instant.ofEpochMilli(createdDate.toInstant().toEpochMilli()));
        setLastModifiedBy(lastModifiedBy);
        setLastModifiedDate(Instant.ofEpochMilli(lastModifiedDate.toInstant().toEpochMilli()));
    }

    public void setProvisioningCriteriaDefinitions(Set<ProvisioningCriteriaDefinition> provisioningCriteriaDefinition) {
        this.provisioningCriteriaDefinition.clear();
        this.provisioningCriteriaDefinition.addAll(provisioningCriteriaDefinition);
    }

    public void setLoanProductProvisioningCriteria(Set<LoanProductProvisionCriteria> loanProductMapping) {
        this.loanProductMapping.clear();
        this.loanProductMapping.addAll(loanProductMapping);
    }

    public Map<String, Object> update(JsonCommand command, List<LoanProduct> loanProducts) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);
        if (command.isChangeInStringParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, criteriaName)) {
            final String valueAsInput = command.stringValueOfParameterNamed(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM);
            actualChanges.put(ProvisioningCriteriaConstants.JSON_CRITERIANAME_PARAM, valueAsInput);
            this.criteriaName = valueAsInput;
        }

        Set<LoanProductProvisionCriteria> temp = new HashSet<>();
        Set<LoanProduct> productsTemp = new HashSet<>();

        for (LoanProductProvisionCriteria mapping : loanProductMapping) {
            if (!loanProducts.contains(mapping.getLoanProduct())) {
                temp.add(mapping);
            } else {
                productsTemp.add(mapping.getLoanProduct());
            }
        }
        loanProductMapping.removeAll(temp);

        for (LoanProduct loanProduct : loanProducts) {
            if (!productsTemp.contains(loanProduct)) {
                this.loanProductMapping.add(new LoanProductProvisionCriteria(this, loanProduct));
            }
        }

        actualChanges.put(ProvisioningCriteriaConstants.JSON_LOANPRODUCTS_PARAM, loanProductMapping);
        return actualChanges;
    }

    public void update(ProvisioningCriteriaDefinitionData data, GLAccount liability, GLAccount expense) {
        for (ProvisioningCriteriaDefinition def : provisioningCriteriaDefinition) {
            if (data.getId().equals(def.getId())) {
                def.update(data.getMinAge(), data.getMaxAge(), data.getProvisioningPercentage(), liability, expense);
                break;
            }
        }
    }
}
