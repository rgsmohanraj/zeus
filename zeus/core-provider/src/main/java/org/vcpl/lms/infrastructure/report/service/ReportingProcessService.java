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
package org.vcpl.lms.infrastructure.report.service;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import net.sf.jasperreports.engine.JRException;
import org.vcpl.lms.infrastructure.security.utils.SQLInjectionValidator;

public interface ReportingProcessService {

    Response processRequest(String reportName, MultivaluedMap<String, String> queryParams) throws FileNotFoundException, JRException, SQLException;

    default Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams) {
        final Map<String, String> reportParams = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        for (final String k : keys) {
            if (k.startsWith("R_")) {
                String pKey = "${" + k.substring(2) + "}";
                String pValue = queryParams.get(k).get(0);
                SQLInjectionValidator.validateSQLInput(pValue);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }
}
