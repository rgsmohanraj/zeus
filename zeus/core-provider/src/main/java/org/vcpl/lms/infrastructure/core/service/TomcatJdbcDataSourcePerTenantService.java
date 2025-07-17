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
package org.vcpl.lms.infrastructure.core.service;

import static org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenantConnection.toJdbcUrl;
import static org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenantConnection.toProtocol;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenantConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Implementation that returns a new or existing connection pool datasource based on the tenant details stored in a
 * {@link ThreadLocal} variable for this request.
 *
 * {@link ThreadLocalContextUtil} is used to retrieve the {@link ZeusPlatformTenant} for the request.
 */
@Service
public class TomcatJdbcDataSourcePerTenantService implements RoutingDataSourceService {

    private final Map<Long, DataSource> tenantToDataSourceMap = new HashMap<>();
    private final DataSource tenantDataSource;

    @Autowired
    private HikariConfig hikariConfig;

    @Autowired
    public TomcatJdbcDataSourcePerTenantService(final @Qualifier("hikariTenantDataSource") DataSource tenantDataSource) {
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public DataSource retrieveDataSource() {
        // default database datasource
        DataSource customTenantDataSource = this.tenantDataSource;
        final ZeusPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        if (tenant != null) {
            final ZeusPlatformTenantConnection tenantConnection = tenant.getConnection();

            synchronized (this.tenantToDataSourceMap) {
                DataSource possibleDS = this.tenantToDataSourceMap.get(tenantConnection.getConnectionId());
                if (possibleDS != null) {
                    customTenantDataSource = possibleDS;
                } else {
                    customTenantDataSource = createNewDataSourceFor(tenantConnection);
                    this.tenantToDataSourceMap.put(tenantConnection.getConnectionId(), customTenantDataSource);
                }
            }
        }

        return customTenantDataSource;
    }

    // creates the tenant data source for the oltp and report database
    private DataSource createNewDataSourceFor(final ZeusPlatformTenantConnection tenantConnectionObj) {
        String protocol = toProtocol(this.tenantDataSource);
        String jdbcUrl = toJdbcUrl(protocol, tenantConnectionObj.getSchemaServer(), tenantConnectionObj.getSchemaServerPort(),
                tenantConnectionObj.getSchemaName(), tenantConnectionObj.getSchemaConnectionParameters());
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(hikariConfig.getDriverClassName());
        config.setPoolName(tenantConnectionObj.getSchemaName() + "_pool");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(tenantConnectionObj.getSchemaUsername());
        config.setPassword(tenantConnectionObj.getSchemaPassword());
        config.setMinimumIdle(tenantConnectionObj.getInitialSize());
        config.setMaximumPoolSize(tenantConnectionObj.getMaxActive());
        config.setIdleTimeout(tenantConnectionObj.getMinEvictableIdleTimeMillis());
        config.setConnectionTestQuery(hikariConfig.getConnectionTestQuery());
        config.setValidationTimeout(tenantConnectionObj.getValidationInterval());
        config.setAutoCommit(hikariConfig.isAutoCommit());
        // https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management
        config.setRegisterMbeans(true);
        // https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
        // These are the properties for each Tenant DB; the same configuration
        // is also in src/main/resources/META-INF/spring/hikariDataSource.xml
        // for the all Tenants DB -->
        config.setDataSourceProperties(hikariConfig.getDataSourceProperties());
        return new HikariDataSource(config);
    }
}
