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
package org.vcpl.lms.portfolio.partner.service;

import java.math.BigDecimal;
import java.util.Map;
import jakarta.persistence.PersistenceException;

import com.google.gson.JsonElement;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vcpl.lms.infrastructure.codes.domain.CodeValue;
import org.vcpl.lms.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.vcpl.lms.infrastructure.core.api.JsonCommand;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
import org.vcpl.lms.infrastructure.core.data.CommandProcessingResultBuilder;
import org.vcpl.lms.infrastructure.core.exception.PlatformDataIntegrityException;
import org.vcpl.lms.infrastructure.core.serialization.FromJsonHelper;
import org.vcpl.lms.infrastructure.security.exception.NoAuthorizationException;
import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
import org.vcpl.lms.notification.service.TopicDomainService;
import org.vcpl.lms.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.vcpl.lms.organisation.office.domain.OfficeRepositoryWrapper;
import org.vcpl.lms.portfolio.partner.domain.Partner;
import org.vcpl.lms.portfolio.partner.domain.PartnerRepositoryWrapper;
import org.vcpl.lms.portfolio.partner.serialization.PartnerCommandFromApiJsonDeserializer;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartnerWritePlatformServiceJpaRepositoryImpl implements PartnerWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(PartnerWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final PartnerCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final PartnerRepositoryWrapper partnerRepositoryWrapper;
    private final TopicDomainService topicDomainService;
    private final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Autowired
    public PartnerWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final PartnerCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final PartnerRepositoryWrapper partnerRepositoryWrapper,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final TopicDomainService topicDomainService, FromJsonHelper fromApiJsonHelper,
            final OfficeRepositoryWrapper officeRepositoryWrapper,
            final CodeValueRepositoryWrapper codeValueRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.partnerRepositoryWrapper = partnerRepositoryWrapper;
        this.topicDomainService = topicDomainService;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.codeValueRepository = codeValueRepository;
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "partners", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "partnersForDropdown", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')") })
    public CommandProcessingResult createPartner(final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final String json=command.json();
            final JsonElement element = this.fromApiJsonHelper.parse(json);

//            final BigDecimal balanceLimitcalc = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("approvedLimit", element);

            CodeValue source = null;
            final String sourceCodeValue = "source";
            final Long sourceId = command.longValueOfParameterNamed(sourceCodeValue);
            if (sourceId != null) {
                source = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(sourceCodeValue, sourceId);
            }


            CodeValue state = null;
            final String stateCodeValue = "state";
            final Long stateId = command.longValueOfParameterNamed(stateCodeValue);
            if (stateId != null) {
                state = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(stateCodeValue, stateId);
            }

            CodeValue country = null;
            final String countryCodeValue = "country";
            final Long countryId = command.longValueOfParameterNamed(countryCodeValue);
            if (countryId != null) {
                country = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(countryCodeValue, countryId);
            }

            CodeValue constitution = null;
            final String constitutionCodeValue = "constitution";
            final Long constitutionId = command.longValueOfParameterNamed(constitutionCodeValue);
            if (constitutionId != null) {
                constitution = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(constitutionCodeValue, constitutionId);
            }

            CodeValue industry = null;
            final String industryCodeValue = "industry";
            final Long industryId = command.longValueOfParameterNamed(industryCodeValue);
            if (industryId != null) {
                industry = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(industryCodeValue, industryId);
            }

            CodeValue sector = null;
            final String sectorCodeValue = "sector";
            final Long sectorId = command.longValueOfParameterNamed(sectorCodeValue);
            if (sectorId != null) {
                sector = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(sectorCodeValue, sectorId);
            }

            CodeValue subSector = null;
            final String subSectorCodeValue = "subSector";
            final Long subSectorId = command.longValueOfParameterNamed(subSectorCodeValue);
            if (subSectorId != null) {
                subSector = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(subSectorCodeValue, subSectorId);
            }

            CodeValue gstRegistration = null;
            final String gstRegistrationCodeValue = "gstRegistration";
            final Long gstRegistrationId = command.longValueOfParameterNamed(gstRegistrationCodeValue);
            if (gstRegistrationId != null) {
                gstRegistration = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(gstRegistrationCodeValue, gstRegistrationId);
            }

            CodeValue partnerType = null;
            final String partnerTypeCodeValue = "partnerType";
            final Long partnerTypeId = command.longValueOfParameterNamed(partnerTypeCodeValue);
            if (partnerTypeId != null) {
                partnerType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(partnerTypeCodeValue, partnerTypeId);
            }

            CodeValue underlyingAssets = null;
            final String underlyingAssetsCodeValue = "underlyingAssets";
            final Long underlyingAssetsId = command.longValueOfParameterNamed(underlyingAssetsCodeValue);
            if (underlyingAssetsId != null) {
                underlyingAssets = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(underlyingAssetsCodeValue, underlyingAssetsId);
            }

            CodeValue security = null;
            final String securityCodeValue = "security";
            final Long securityId = command.longValueOfParameterNamed(securityCodeValue);
            if (securityId != null) {
                security = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(securityCodeValue, securityId);
            }

            CodeValue fldgCalculationOn = null;
            final String fldgCalculationOnCodeValue = "fldgCalculationOn";
            final Long fldgCalculationOnId = command.longValueOfParameterNamed(fldgCalculationOnCodeValue);
            if (fldgCalculationOnId != null) {
                fldgCalculationOn = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(fldgCalculationOnCodeValue, fldgCalculationOnId);
            }

//            final BigDecimal balanceLimit = balanceLimitcalc;
            final Partner partner = Partner.fromJson(command, source, state, country,
                    constitution, industry, sector, subSector, gstRegistration, partnerType,
                    underlyingAssets, security, fldgCalculationOn);

            // pre save to generate id for use in partner hierarchy
            this.partnerRepositoryWrapper.saveAndFlush(partner);

            this.partnerRepositoryWrapper.save(partner);

//            this.topicDomainService.createTopic(partner);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(partner.getId()) //
                    .withPartnerId(partner.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handlePartnerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handlePartnerDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "partners", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')"),
            @CacheEvict(value = "partnersForDropdown", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'ofd')"),
            @CacheEvict(value = "partnersById", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#partnerId)") })
    public CommandProcessingResult updatePartner(final Long partnerId, final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Partner partnerForUpdate = this.partnerRepositoryWrapper.findOneWithNotFoundDetection(partnerId);

            final Partner partner = this.partnerRepositoryWrapper.findOneWithNotFoundDetection(partnerId);

            final Map<String, Object> changes = partner.update(command);

            if (changes.containsKey("source")) {
                final Long newValue = command.longValueOfParameterNamed("source");
                final String sourceCodeValue = "source";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(sourceCodeValue, newValue);
                }
                partnerForUpdate.updateSource(newCodeVal);
            }



            if (changes.containsKey("state")) {
                final Long newValue = command.longValueOfParameterNamed("state");
                final String stateCodeValue = "state";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(stateCodeValue, newValue);
                }
                partnerForUpdate.updateState(newCodeVal);
            }

            if (changes.containsKey("country")) {
                final Long newValue = command.longValueOfParameterNamed("country");
                final String countryCodeValue = "country";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(countryCodeValue, newValue);
                }
                partnerForUpdate.updateCountry(newCodeVal);
            }

            if (changes.containsKey("constitution")) {
                final Long newValue = command.longValueOfParameterNamed("constitution");
                final String constitutionCodeValue = "constitution";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(constitutionCodeValue, newValue);
                }
                partnerForUpdate.updateConstitution(newCodeVal);
            }

            if (changes.containsKey("industry")) {
                final Long newValue = command.longValueOfParameterNamed("industry");
                final String industryCodeValue = "industry";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(industryCodeValue, newValue);
                }
                partnerForUpdate.updateIndustry(newCodeVal);
            }

            if (changes.containsKey("sector")) {
                final Long newValue = command.longValueOfParameterNamed("sector");
                final String sectorCodeValue = "sector";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(sectorCodeValue, newValue);
                }
                partnerForUpdate.updateSector(newCodeVal);
            }

            if (changes.containsKey("subSector")) {
                final Long newValue = command.longValueOfParameterNamed("subSector");
                final String subSectorCodeValue = "subSector";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(subSectorCodeValue, newValue);
                }
                partnerForUpdate.updateSubSector(newCodeVal);
            }

            if (changes.containsKey("gstRegistration")) {
                final Long newValue = command.longValueOfParameterNamed("gstRegistration");
                final String gstRegistrationCodeValue = "gstRegistration";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(gstRegistrationCodeValue, newValue);
                }
                partnerForUpdate.updateGstRegistration(newCodeVal);
            }

            if (changes.containsKey("partnerType")) {
                final Long newValue = command.longValueOfParameterNamed("partnerType");
                final String partnerTypeCodeValue = "partnerType";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(partnerTypeCodeValue, newValue);
                }
                partnerForUpdate.updatePartnerType(newCodeVal);
            }

            if (changes.containsKey("underlyingAssets")) {
                final Long newValue = command.longValueOfParameterNamed("underlyingAssets");
                final String underlyingAssetsCodeValue = "underlyingAssets";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(underlyingAssetsCodeValue, newValue);
                }
                partnerForUpdate.updateUnderlyingAssets(newCodeVal);
            }

            if (changes.containsKey("security")) {
                final Long newValue = command.longValueOfParameterNamed("security");
                final String securityCodeValue = "security";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(securityCodeValue, newValue);
                }
                partnerForUpdate.updateSecurity(newCodeVal);
            }

            if (changes.containsKey("fldgCalculationOn")) {
                final Long newValue = command.longValueOfParameterNamed("fldgCalculationOn");
                final String fldgCalculationOnCodeValue = "fldgCalculationOn";
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(fldgCalculationOnCodeValue, newValue);
                }
                partnerForUpdate.updateFldgCalculationOn(newCodeVal);
            }


            if (!changes.isEmpty()) {
                this.partnerRepositoryWrapper.saveAndFlush(partner);

//                this.topicDomainService.updateTopic(partner, changes);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(partner.getId()) //
                    .withPartnerId(partner.getId()) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handlePartnerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handlePartnerDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handlePartnerDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("externalid_org")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.partner.duplicate.externalId",
                    "Partner with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.partner.duplicate.name", "Partner with name `" + name + "` already exists",
                    "name", name);
        }

        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.partner.unknown.data.integrity.issue",
                realCause.getMessage());
  //              "Unknown data integrity issue with resource.");
    }

    /*
     * used to restrict modifying operations to partner that are either the users partner or lower (child) in the partner
     * hierarchy
     */
    private Partner validateUserPriviledgeOnPartnerAndRetrieve(final Long partnerId) {

        //final Long userOfficeId = currentUser.getOffice().getId();
        final Partner userPartner = this.partnerRepositoryWrapper.findOneWithNotFoundDetection(partnerId);
        if (userPartner.doesNotHaveAnPartnerInHierarchyWithId(partnerId)) {
            throw new NoAuthorizationException("User does not have sufficient priviledges to act on the provided partner.");
        }

        Partner partnerToReturn = userPartner ;
        if (!userPartner.identifiedBy(partnerId)) {
            partnerToReturn = this.partnerRepositoryWrapper.findPartnerHierarchy(partnerId);
        }

        return partnerToReturn;
    }

    public PlatformSecurityContext getContext() {
        return this.context;
    }
}
