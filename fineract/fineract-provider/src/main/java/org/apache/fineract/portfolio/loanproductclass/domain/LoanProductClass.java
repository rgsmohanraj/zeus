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
package org.apache.fineract.portfolio.loanproductclass.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "m_loan_product_class", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "loan_product_class_name_org")})
public class LoanProductClass extends AbstractPersistableCustom {

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 500)
    private String description;



    public static LoanProductClass fromJson(final JsonCommand command) {

        final String firstnameParamName = "name";
        final String name = command.stringValueOfParameterNamed(firstnameParamName);

        final String lastnameParamName = "description";
        final String description = command.stringValueOfParameterNamed(lastnameParamName);

        return new LoanProductClass(name, description);
    }
    protected LoanProductClass() {
        //
    }

    private LoanProductClass(final String loanProductClassName, final String description) {
        this.name = StringUtils.defaultIfEmpty(loanProductClassName, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
    }
    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

}
