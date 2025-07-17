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
package org.vcpl.lms.infrastructure.core.domain;

import java.sql.Connection;
import javax.sql.DataSource;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Holds Tenant's DB server connection connection details.
 */
@Getter
public class ZeusPlatformTenantConnection {
    private Long connectionId;
    private String schemaServer;
    private String schemaServerPort;
    private String schemaConnectionParameters;
    private String schemaUsername;
    private String schemaPassword;
    private String schemaName;
    private boolean autoUpdateEnabled;
    private int initialSize;
    private long validationInterval;
    private boolean removeAbandoned;
    private int removeAbandonedTimeout;
    private boolean logAbandoned;
    private int abandonWhenPercentageFull;
    private int maxActive;
    private int minIdle;
    private int maxIdle;
    private int suspectTimeout;
    private int timeBetweenEvictionRunsMillis;
    private int minEvictableIdleTimeMillis;
    private int maxRetriesOnDeadlock;
    private int maxIntervalBetweenRetries;
    private boolean testOnBorrow;
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.schemaName).append(":").append(this.schemaServer).append(":")
                .append(this.schemaServerPort);
        if (this.schemaConnectionParameters != null && !this.schemaConnectionParameters.isEmpty()) {
            sb.append('?').append(this.schemaConnectionParameters);
        }
        return sb.toString();
    }

    public static String toJdbcUrl(String protocol, String host, String port, String db, String parameters) {
        StringBuilder sb = new StringBuilder(protocol).append("://").append(host).append(":").append(port).append('/').append(db);

        if (!StringUtils.isEmpty(parameters)) {
            sb.append('?').append(parameters);
        }

        return sb.toString();
    }

    public static String toProtocol(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return url.substring(0, url.indexOf("://"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
