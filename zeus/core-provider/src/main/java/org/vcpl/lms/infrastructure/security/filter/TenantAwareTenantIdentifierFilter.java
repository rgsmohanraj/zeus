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
package org.vcpl.lms.infrastructure.security.filter;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.vcpl.lms.infrastructure.cache.domain.CacheType;
import org.vcpl.lms.infrastructure.cache.service.CacheWritePlatformService;
import org.vcpl.lms.infrastructure.configuration.domain.ConfigurationDomainService;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.serialization.ToApiJsonSerializer;
import org.vcpl.lms.infrastructure.core.service.TenantPropertiesHandlerService;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.security.data.PlatformRequestLog;
import org.vcpl.lms.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.vcpl.lms.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * This filter is responsible for extracting multi-tenant from the request and setting Cross-Origin details to response.
 *
 * If multi-tenant are valid, the details of the tenant are stored in {@link ZeusPlatformTenant} and stored in a
 * {@link ThreadLocal} variable for this request using {@link ThreadLocalContextUtil}.
 *
 * If multi-tenant are invalid, a http error response is returned.
 *
 * Used to support Oauth2 authentication and the service is loaded only when "oauth" profile is active.
 */
@Service
@ConditionalOnProperty("lms.security.oauth.enabled")
public class TenantAwareTenantIdentifierFilter extends GenericFilterBean {

    private static boolean firstRequestProcessed = false;
    private static final Logger LOG = LoggerFactory.getLogger(TenantAwareTenantIdentifierFilter.class);

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
    private final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CacheWritePlatformService cacheWritePlatformService;

    private final TenantPropertiesHandlerService tenantPropertiesHandlerService;
    private final String tenantRequestHeader = "Zeus-Platform-TenantId";
    private final boolean exceptionIfHeaderMissing = true;
    private final String apiUri = "/api/v1/";

    @Autowired
    public TenantAwareTenantIdentifierFilter(final BasicAuthTenantDetailsService basicAuthTenantDetailsService,
                                             final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer, final ConfigurationDomainService configurationDomainService,
                                             final CacheWritePlatformService cacheWritePlatformService, TenantPropertiesHandlerService tenantPropertiesHandlerService) {
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.configurationDomainService = configurationDomainService;
        this.cacheWritePlatformService = cacheWritePlatformService;
        this.tenantPropertiesHandlerService = tenantPropertiesHandlerService;
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final StopWatch task = new StopWatch();
        task.start();

        try {

            // allows for Cross-Origin
            // Requests (CORs) to be performed against the platform API.
            response.setHeader("Access-Control-Allow-Origin", "*"); // NOSONAR
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            final String reqHead = request.getHeader("Access-Control-Request-Headers");

            if (null != reqHead && !reqHead.isEmpty()) {
                response.setHeader("Access-Control-Allow-Headers", reqHead);
            }

            if(isSwaggerRequest(request)){
                chain.doFilter(request, response);
                return;
            }

            if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
                String tenantIdentifier = request.getHeader(this.tenantRequestHeader);
                if (StringUtils.isEmpty(tenantIdentifier)) {
                    throw new InvalidTenantIdentiferException("No tenant identifier found: in tenantPropertyHandler object unable to find the tenant name '"
                            + tenantIdentifier + "' or add the parameter 'tenantIdentifier' to query string of request URL.");
                }
                //based on the tenant connection information is get, it is a singleton instance
                ZeusPlatformTenant tenantDetails  = tenantPropertiesHandlerService.getInstance().getTenantPlatformDetails().get(tenantIdentifier);
                if(Objects.isNull(tenantDetails)){
                    throw new InvalidTenantIdentiferException("Tenant not found, identifier name:-".concat(tenantIdentifier).concat("-:Check whether we have the configuration for the particular tenant"));
                }
                ThreadLocalContextUtil.setTenant(tenantDetails);
                String authToken = request.getHeader("Authorization");
                if (authToken != null && authToken.startsWith("bearer ")) {
                    ThreadLocalContextUtil.setAuthToken(authToken.replaceFirst("bearer ", ""));
                }
                if (!firstRequestProcessed) {
                    final String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(),
                            request.getContextPath() + apiUri);
                    System.setProperty("baseUrl", baseUrl);
                    final boolean ehcacheEnabled = this.configurationDomainService.isEhcacheEnabled();
                    if (ehcacheEnabled) {
                        this.cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
                    } else {
                        this.cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
                    }
                    TenantAwareTenantIdentifierFilter.firstRequestProcessed = true;
                }
                chain.doFilter(request, response);
            }
        } catch (final InvalidTenantIdentiferException e) {
            // deal with exception at low level
            SecurityContextHolder.getContext().setAuthentication(null);

            response.addHeader("WWW-Authenticate", "Basic realm=\"" + "Fineract Platform API" + "\"");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            task.stop();
            final PlatformRequestLog log = PlatformRequestLog.from(task, request);
        }

    }
    private boolean isSwaggerRequest(HttpServletRequest request){

        if(request.getRequestURI().contains("swagger") || request.getRequestURI().contains("lms.json")){
            return true;
        }
        return false;
    }
}
