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
import org.vcpl.lms.notification.service.NotificationReadPlatformService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * A customised version of spring security's {@link BasicAuthenticationFilter}.
 *
 * This filter is responsible for extracting multi-tenant and basic auth credentials from the request and checking that
 * the details provided are valid.
 *
 * If multi-tenant and basic auth credentials are valid, the details of the tenant are stored in
 * {@link ZeusPlatformTenant} and stored in a {@link ThreadLocal} variable for this request using
 * {@link ThreadLocalContextUtil}.
 *
 * If multi-tenant and basic auth credentials are invalid, a http error response is returned.
 */

@ConditionalOnProperty("lms.security.basicauth.enabled")
public class TenantAwareBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private static boolean firstRequestProcessed = false;
    private static final Logger LOG = LoggerFactory.getLogger(TenantAwareBasicAuthenticationFilter.class);

    @Autowired
    private ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;

    @Autowired
    private ConfigurationDomainService configurationDomainService;

    @Autowired
    private CacheWritePlatformService cacheWritePlatformService;

    @Autowired
    private NotificationReadPlatformService notificationReadPlatformService;

    @Autowired
    private BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    @Autowired
    private TenantPropertiesHandlerService tenantPropertiesHandlerService;

    private final String tenantRequestHeader = "Zeus-Platform-TenantId";

    public TenantAwareBasicAuthenticationFilter(final AuthenticationManager authenticationManager,
            final AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager, authenticationEntryPoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final StopWatch task = new StopWatch();
        task.start();
        try {
            response.setHeader("Access-Control-Allow-Origin", "*"); // NOSONAR
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            final String reqHead = request.getHeader("Access-Control-Request-Headers");

            if (null != reqHead && !reqHead.isEmpty()) {
                response.setHeader("Access-Control-Allow-Headers", reqHead);
            }

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                // ignore to allow 'preflight' requests from AJAX applications
                // in different origin (domain name)
            } else {
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
                if (authToken != null && authToken.startsWith("Basic ")) {
                    ThreadLocalContextUtil.setAuthToken(authToken.replaceFirst("Basic ", ""));
                }
                if (!firstRequestProcessed) {
                    final String baseUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "/");
                    System.setProperty("baseUrl", baseUrl);
                    final boolean ehcacheEnabled = this.configurationDomainService.isEhcacheEnabled();
                    if (ehcacheEnabled) {
                        this.cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
                    } else {
                        this.cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
                    }
                    TenantAwareBasicAuthenticationFilter.firstRequestProcessed = true;
                }
            }

            super.doFilterInternal(request, response, filterChain);
        } catch (final InvalidTenantIdentiferException e) {
            // deal with exception at low level
            SecurityContextHolder.getContext().setAuthentication(null);

            response.addHeader("WWW-Authenticate", "Basic realm=\"" + "Fineract Platform API" + "\"");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            task.stop();
            final PlatformRequestLog log = PlatformRequestLog.from(task, request);
            LOG.debug("{}", this.toApiJsonSerializer.serialize(log));
        }
    }
    private String getDefaultTenantName(String tenantsName){
        if(StringUtils.isEmpty(tenantsName)){
            return StringUtils.EMPTY;
        }
        return tenantsName.split(",")[0];
    }
    @Override
    protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult)
            throws IOException {
        super.onSuccessfulAuthentication(request, response, authResult);
        AppUser user = (AppUser) authResult.getPrincipal();

        if (notificationReadPlatformService.hasUnreadNotifications(user.getId())) {
            response.addHeader("X-Notification-Refresh", "true");
        } else {
            response.addHeader("X-Notification-Refresh", "false");
        }

        String pathURL = request.getRequestURI();
        boolean isSelfServiceRequest = pathURL != null && pathURL.contains("/self/");

        boolean notAllowed = (isSelfServiceRequest && !user.isSelfServiceUser()) || (!isSelfServiceRequest && user.isSelfServiceUser());

        if (notAllowed) {
            throw new BadCredentialsException("User not authorised to use the requested resource.");
        }
    }
}
