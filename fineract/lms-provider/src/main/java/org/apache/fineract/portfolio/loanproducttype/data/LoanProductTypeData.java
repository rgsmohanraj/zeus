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
package org.apache.fineract.portfolio.loanproducttype.data;

import java.io.Serializable;

/**
 * Immutable data object to represent type data.
 */

public final class LoanProductTypeData implements Serializable {
    private final Long id;

   private final Long classId;

    private final String name;

    private final String description;

    public static LoanProductTypeData instance(final Long id, final String name, final String description,final Long classId) {
        return new LoanProductTypeData(id, name, description, classId);
    }
    private LoanProductTypeData(final Long id, final String name, final String description,final Long classId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.classId=classId;
    }
    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
    public String getDescription(){
        return description;
    }
public Long getClassId(){
        return this.classId;
}

}
