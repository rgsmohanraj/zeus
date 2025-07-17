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
package org.vcpl.lms.organisation.office.domain;

import org.vcpl.lms.infrastructure.codes.data.CodeValueData;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.organisation.office.data.OfficeGstData;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "m_office_gst_details")
public class OfficeGst extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "gst_number", nullable = true)
    private String gstNumber;

    @ManyToOne
    @JoinColumn(name = "state_cv_id", nullable = true)
    private CodeValue state;

    @Column(name = "cgst", nullable = true, scale = 19, precision = 6)
    private BigDecimal cgst;

    @Column(name = "sgst", nullable = true, scale = 19, precision = 6)
    private BigDecimal sgst;

    @Column(name = "igst", nullable = true, scale = 19, precision = 6)
    private BigDecimal igst;

    protected OfficeGst() {
    }

    public OfficeGst(final String gstNumber, final CodeValue state,
                     final BigDecimal cgst, final BigDecimal sgst, final BigDecimal igst) {

//        this.office = office;
        this.gstNumber = gstNumber;
        this.state = state;
        this.cgst = cgst;
        this.sgst = sgst;
        this.igst = igst;
    }

    public String getGstNumber() {
        return this.gstNumber;
    }

    public CodeValue getState() {
        return this.state;
    }

    public BigDecimal getCgst() {
        return this.cgst;
    }

    public BigDecimal getSgst() {
        return this.sgst;
    }

    public BigDecimal getIgst() {
        return this.igst;
    }

//    public static Set<OfficeGst> officeGsts(Office office, List<OfficeGstData> officeGstData) {
//
//        Set<OfficeGst> officeGsts = new HashSet<>();
//
//        if (officeGstData.isEmpty()) {
//            return null;
//        } else {
//            for (OfficeGstData officeGstDatas : officeGstData)
//            {
//                final String gstNumber = officeGstDatas.getGstNumber();
//                final CodeValue state = officeGstDatas.getState();
//                final BigDecimal cgst = officeGstDatas.getCgst();
//                final BigDecimal sgst = officeGstDatas.getSgst();
//                final BigDecimal igst = officeGstDatas.getIgst();
//
//                OfficeGst officeGst = new OfficeGst(office, gstNumber, state, cgst, sgst, igst);
//                officeGsts.add(officeGst);
//            }
//        }
//
//        return officeGsts;
//    }

    public void updateOffice(final Office office) { this.office = office; }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof OfficeGst)) {
            return false;
        }
        final OfficeGst other = (OfficeGst) obj;
        return Objects.equals(office, other.office) && Objects.equals(gstNumber, other.gstNumber)
                && Objects.equals(state, other.state) && Objects.equals(cgst, other.cgst) && Objects.equals(sgst, other.sgst)
                && Objects.equals(igst, other.igst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(office, gstNumber, state, cgst, sgst, igst);
    }

    public void copy(final OfficeGst gsts) {
        this.gstNumber = gsts.gstNumber;
        this.state = gsts.state;
        this.cgst = gsts.cgst;
        this.sgst = gsts.sgst;
        this.igst = gsts.igst;
    }

}
