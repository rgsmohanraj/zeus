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
package org.vcpl.lms.infrastructure.core.service.migration;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.vcpl.lms.infrastructure.core.config.LMSProperties;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.service.TenantPropertiesHandlerService;
import org.vcpl.lms.infrastructure.security.service.TenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * A service that picks up on tenants that are configured to auto-update their specific schema on application startup.
 */
@Service
public class TenantDatabaseUpgradeService implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(TenantDatabaseUpgradeService.class);
    private static final String TENANT_STORE_DB_CONTEXT = "tenant_store_db";
    private static final String INITIAL_SWITCH_CONTEXT = "initial_switch";
    private static final String TENANT_DB_CONTEXT = "tenant_db";

    private final TenantDetailsService tenantDetailsService;
    private final DataSource tenantDataSource;
    private final LMSProperties lmsProperties;
    private final TenantDatabaseStateVerifier databaseStateVerifier;
    private final ExtendedSpringLiquibaseFactory liquibaseFactory;
    private final TenantDataSourceFactory tenantDataSourceFactory;

    @Autowired
    public TenantDatabaseUpgradeService(final TenantDetailsService detailsService,
            @Qualifier("hikariTenantDataSource") final DataSource tenantDataSource, final LMSProperties lmsProperties,
            TenantDatabaseStateVerifier databaseStateVerifier, ExtendedSpringLiquibaseFactory liquibaseFactory,
            TenantDataSourceFactory tenantDataSourceFactory) {
        this.tenantDetailsService = detailsService;
        this.tenantDataSource = tenantDataSource;
        this.lmsProperties = lmsProperties;
        this.databaseStateVerifier = databaseStateVerifier;
        this.liquibaseFactory = liquibaseFactory;
        this.tenantDataSourceFactory = tenantDataSourceFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (databaseStateVerifier.isLiquibaseDisabled() || !lmsProperties.getMode().isWriteEnabled()) {
            LOG.warn("Liquibase is disabled. Not upgrading any database.");
            if (!lmsProperties.getMode().isWriteEnabled()) {
                LOG.warn("Liquibase is disabled because the current instance is configured as a non-write LMS instance");
            }
            return;
        }
        try {
//            not creating the tenant schema
//            upgradeTenantStore();
            upgradeIndividualTenants();
        } catch (LiquibaseException e) {
            throw new RuntimeException("Error while migrating the schema", e);
        }
    }

//    private void upgradeTenantStore() throws LiquibaseException {
//        LOG.warn("Upgrading tenant store DB at {}:{}", lmsProperties.getTenant().getHost(), lmsProperties.getTenant().getPort());
//        logTenantStoreDetails();
//        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
//            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT, INITIAL_SWITCH_CONTEXT);
//            applyInitialLiquibase(tenantDataSource, liquibase, "tenant store",
//                    (ds) -> !databaseStateVerifier.isTenantStoreOnLatestUpgradableVersion(ds));
//        }
//        SpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_STORE_DB_CONTEXT);
//        liquibase.afterPropertiesSet();
//        LOG.warn("Tenant store upgrade finished");
//    }

//    private void logTenantStoreDetails() {
//        LOG.debug("- lms.tenant.username: {}", lmsProperties.getTenant().getUsername());
//        LOG.debug("- lms.tenant.password: ****");
//        LOG.debug("- lms.tenant.parameters: {}", lmsProperties.getTenant().getParameters());
//        LOG.debug("- lms.tenant.timezone: {}", lmsProperties.getTenant().getTimezone());
//        LOG.debug("- lms.tenant.description: {}", lmsProperties.getTenant().getDescription());
//        LOG.debug("- lms.tenant.identifier: {}", lmsProperties.getTenant().getIdentifier());
//        LOG.debug("- lms.tenant.name: {}", lmsProperties.getTenant().getName());
//    }

    private void upgradeIndividualTenants() throws LiquibaseException {
        LOG.warn("Upgrading all tenants");
        List<ZeusPlatformTenant> tenants = tenantDetailsService.findAllTenants();
        if (isNotEmpty(tenants)) {
            for (ZeusPlatformTenant tenant : tenants) {
                if(Objects.isNull(tenant))
                    continue;
                upgradeIndividualTenant(tenant);
            }
        }
        LOG.warn("Tenant upgrades have finished");
    }

    private void upgradeIndividualTenant(ZeusPlatformTenant tenant) throws LiquibaseException {
        LOG.info("Upgrade for tenant {} has started", tenant.getTenantIdentifier());
        DataSource tenantDataSource = tenantDataSourceFactory.create(tenant);
        if (databaseStateVerifier.isFirstLiquibaseMigration(tenantDataSource)) {
            ExtendedSpringLiquibase liquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT, INITIAL_SWITCH_CONTEXT);
            applyInitialLiquibase(tenantDataSource, liquibase, tenant.getTenantIdentifier(),
                    (ds) -> !databaseStateVerifier.isTenantOnLatestUpgradableVersion(ds));
        }
        SpringLiquibase tenantLiquibase = liquibaseFactory.create(tenantDataSource, TENANT_DB_CONTEXT);
        tenantLiquibase.afterPropertiesSet();
        LOG.info("Upgrade for tenant {} has finished", tenant.getTenantIdentifier());
    }

    private void applyInitialLiquibase(DataSource dataSource, ExtendedSpringLiquibase liquibase, String id,
            Function<DataSource, Boolean> isUpgradableFn) throws LiquibaseException {
        if (databaseStateVerifier.isFlywayPresent(dataSource)) {
            if (isUpgradableFn.apply(dataSource)) {
                LOG.warn("Cannot proceed with upgrading database {}", id);
                LOG.warn("It seems the database doesn't have the latest schema changes applied until the 1.6 release");
                throw new SchemaUpgradeNeededException("Make sure to upgrade to LMS 1.6 first and then to a newer version");
            }
            LOG.warn("This is the first Liquibase migration for {}. We'll sync the changelog for you and then apply everything else", id);
            liquibase.changeLogSync();
            LOG.warn("Liquibase changelog sync is complete");
        } else {
            liquibase.afterPropertiesSet();
        }
    }
}
