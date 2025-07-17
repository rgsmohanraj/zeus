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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.domain.AbstractPersistableCustom;
import org.vcpl.lms.infrastructure.core.service.DateUtils;
import org.vcpl.lms.organisation.office.data.OfficeGstData;
import org.vcpl.lms.organisation.office.exception.CannotUpdateOfficeWithParentOfficeSameAsSelf;
import org.vcpl.lms.organisation.office.exception.RootOfficeParentCannotBeUpdated;
import org.vcpl.lms.portfolio.charge.domain.Charge;

@Entity
@Table(name = "m_office", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_org"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "externalid_org") })
public class Office extends AbstractPersistableCustom implements Serializable {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private List<Office> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Office parent;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "hierarchy", nullable = true, length = 50)
    private String hierarchy;

    @Column(name = "opening_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date openingDate;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "office", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<OfficeGst> gsts = new HashSet<>();

    public static Office headOffice(final String name, final LocalDate openingDate, final String externalId) {
        return new Office(null, name, openingDate, externalId, null);
    }

    public static Office fromJson(final Office parentOffice, final JsonCommand command, final Set<OfficeGst> officeGstList) {

        final String name = command.stringValueOfParameterNamed("name");
        final LocalDate openingDate = command.localDateValueOfParameterNamed("openingDate");
        final String externalId = command.stringValueOfParameterNamed("externalId");

//        final JsonArray officeGst = command.arrayOfParameterNamed("officeGsts");
//        final Set<OfficeGst> officeGstList = new HashSet<>();
//
//        if(officeGst != null) {
//            assempleOfficeGsts(command, officeGstList);
//        }



        return new Office(parentOffice, name, openingDate, externalId, officeGstList);
    }

    protected Office() {
        this.openingDate = null;
        this.parent = null;
        this.name = null;
        this.externalId = null;
        this.gsts = null;
    }

    private Office(final Office parent, final String name, final LocalDate openingDate, final String externalId,
                   final Set<OfficeGst> officeGstList) {
        this.parent = parent;
        this.openingDate = Date.from(openingDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (parent != null) {
            this.parent.addChild(this);
        }

        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }

        this.gsts = officeGstList;
        for (OfficeGst gsts : this.gsts) {
            gsts.updateOffice(this);
        }
    }

    private void addChild(final Office office) {
        this.children.add(office);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String parentIdParamName = "parentId";

        if (command.parameterExists(parentIdParamName) && this.parent == null) {
            throw new RootOfficeParentCannotBeUpdated();
        }

        if (this.parent != null && command.isChangeInLongParameterNamed(parentIdParamName, this.parent.getId())) {
            final Long newValue = command.longValueOfParameterNamed(parentIdParamName);
            actualChanges.put(parentIdParamName, newValue);
        }

        final String openingDateParamName = "openingDate";
        if (command.isChangeInLocalDateParameterNamed(openingDateParamName, getOpeningLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(openingDateParamName);
            actualChanges.put(openingDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(openingDateParamName);
            this.openingDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String officeGstsParamName = "officeGsts";
        if (command.hasParameter(officeGstsParamName)) {
            actualChanges.putAll(updateOfficeGsts(command));
        }

        return actualChanges;
    }

    public boolean isOpeningDateBefore(final LocalDate baseDate) {
        return getOpeningLocalDate().isBefore(baseDate);
    }

    public boolean isOpeningDateAfter(final LocalDate activationLocalDate) {
        return getOpeningLocalDate().isAfter(activationLocalDate);
    }

    public LocalDate getOpeningLocalDate() {
        LocalDate openingLocalDate = null;
        if (this.openingDate != null) {
            openingLocalDate = LocalDate.ofInstant(this.openingDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
        }
        return openingLocalDate;
    }

    public void update(final Office newParent) {

        if (this.parent == null) {
            throw new RootOfficeParentCannotBeUpdated();
        }

        if (identifiedBy(newParent.getId())) {
            throw new CannotUpdateOfficeWithParentOfficeSameAsSelf(getId(), newParent.getId());
        }

        this.parent = newParent;
        generateHierarchy();
    }

    public boolean update(final Set<OfficeGst> officeGsts ) {

        if (this.parent == null) {
            throw new RootOfficeParentCannotBeUpdated();
        }

        if (officeGsts == null) {
            return false;
        }

        boolean updated = false;
        if (this.gsts != null) {
            final Set<OfficeGst> currentOfficeGst = new HashSet<>(this.gsts);
            //final Set<OfficeGst> newSetOfOfficeGst = new HashSet<>(officeGsts);

            if (!currentOfficeGst.equals(officeGsts)) {
                updated = true;
                this.gsts = officeGsts;
            }
        } else {
            updated = true;
            this.gsts = officeGsts;
        }
        return updated;
    }

    public boolean identifiedBy(final Long id) {
        return getId().equals(id);
    }

    public void generateHierarchy() {

        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public String getName() {
        return this.name;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }

    public Office getParent() {
        return this.parent;
    }

    public boolean hasParentOf(final Office office) {
        if (this.parent != null) {
            return this.parent.equals(office);
        }
        return false;
    }

    public boolean doesNotHaveAnOfficeInHierarchyWithId(final Long officeId) {
        return !hasAnOfficeInHierarchyWithId(officeId);
    }

    private boolean hasAnOfficeInHierarchyWithId(final Long officeId) {

        boolean match = false;

        if (identifiedBy(officeId)) {
            match = true;
        }

        if (!match) {
            for (final Office child : this.children) {
                final boolean result = child.hasAnOfficeInHierarchyWithId(officeId);

                if (result) {
                    match = true;
                    break;
                }
            }
        }

        return match;
    }

    private void updateOfficeGsts(final JsonCommand command, final Map<String, Object> actualChanges,
                                  final String officeGstsParamName, List<Long> officeGstIds) {

        if (command.parameterExists(officeGstsParamName)) {
            final JsonArray officeGstsArray = command.arrayOfParameterNamed("officeGsts");
            if (officeGstsArray != null && officeGstsArray.size() > 0) {
                int i = 0;
                do{
                    final JsonObject jsonObject = officeGstsArray.get(i).getAsJsonObject();

                    Long id = null;
                    String gstNumber = null;
                    BigDecimal cgst = null;
                    BigDecimal sgst = null;
                    BigDecimal igst = null;
//                    final CodeValue state = this.state;

                    if(jsonObject.has("gstNumber") && jsonObject.get("gstNumber").isJsonPrimitive()) {
                        gstNumber = jsonObject.getAsJsonPrimitive("gstNumber").getAsString();
                    }

//                    CodeValue state = null;
//                    final String stateCodeValue = "state";
//                    final Long stateId = jsonObject.get("state").getAsLong();
//                    if (stateId != null) {
//                        state = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(stateCodeValue, stateId);
//                    }

                    if(jsonObject.has("cgst") && jsonObject.get("cgst").isJsonPrimitive()) {
                        cgst = jsonObject.getAsJsonPrimitive("cgst").getAsBigDecimal();
                    }

                    if(jsonObject.has("sgst") && jsonObject.get("sgst").isJsonPrimitive()) {
                        sgst = jsonObject.getAsJsonPrimitive("sgst").getAsBigDecimal();
                    }

                    if(jsonObject.has("igst") && jsonObject.get("igst").isJsonPrimitive()) {
                        igst = jsonObject.getAsJsonPrimitive("igst").getAsBigDecimal();
                    }

                    OfficeGst gsts = new OfficeGst(gstNumber, null, cgst, sgst, igst);

                    if (id == null) {
                        gsts.updateOffice(this);
                        this.gsts.add(gsts);
                        actualChanges.put(officeGstsParamName, command.jsonFragment(officeGstsParamName));
                    } else {
                        officeGstIds.remove(id);
                        OfficeGst existingOfficegsts = fetchOfficeGstById(id);
                        if (!existingOfficegsts.equals(gsts)) {
                            existingOfficegsts.copy(gsts);
                            actualChanges.put("id", id);
                        }
                    }
                    i++;
                } while (i > officeGstsArray.size());

            }
        }
    }

    public Map<String, Object> updateOfficeGsts(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>();
        List<Long> officeGstsIds = fetchAllGstIds();
        final String officeGstsParamName = "officeGsts";
        updateOfficeGsts(command, actualChanges, officeGstsParamName, officeGstsIds);
        for (Long id : officeGstsIds) {
            this.gsts.remove(fetchOfficeGstById(id));
        }
        return actualChanges;
    }

    public List<Long> fetchAllGstIds() {
        List<Long> list = new ArrayList<>();
        for (OfficeGst gsdIds : this.gsts) {
            list.add(gsdIds.getId());
        }
        return list;
    }

    public OfficeGst fetchOfficeGstById(Long id) {
        OfficeGst officeGst = null;
        for (OfficeGst gstIds : this.gsts) {
            if (id.equals(gstIds.getId())) {
                officeGst = gstIds;
                break;
            }
        }
        return officeGst;
    }

    public void loadLazyCollections() {
        this.children.size();
    }
}
