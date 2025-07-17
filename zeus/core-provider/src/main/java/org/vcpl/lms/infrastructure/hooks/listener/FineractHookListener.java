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
package org.vcpl.lms.infrastructure.hooks.listener;

import java.util.List;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.hooks.domain.Hook;
import org.vcpl.lms.infrastructure.hooks.event.HookEvent;
import org.vcpl.lms.infrastructure.hooks.event.HookEventSource;
import org.vcpl.lms.infrastructure.hooks.processor.HookProcessor;
import org.vcpl.lms.infrastructure.hooks.processor.HookProcessorProvider;
import org.vcpl.lms.infrastructure.hooks.service.HookReadPlatformService;
import org.vcpl.lms.infrastructure.security.service.TenantDetailsService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FineractHookListener implements HookListener {

    private static final Logger LOG = LoggerFactory.getLogger(FineractHookListener.class);

    private final HookProcessorProvider hookProcessorProvider;
    private final HookReadPlatformService hookReadPlatformService;
    private final TenantDetailsService tenantDetailsService;

    @Autowired
    public FineractHookListener(final HookProcessorProvider hookProcessorProvider, final HookReadPlatformService hookReadPlatformService,
            final TenantDetailsService tenantDetailsService) {
        this.hookReadPlatformService = hookReadPlatformService;
        this.hookProcessorProvider = hookProcessorProvider;
        this.tenantDetailsService = tenantDetailsService;
    }

    @Override
    public void onApplicationEvent(final HookEvent event) {
        final String tenantIdentifier = event.getTenantIdentifier();
        final ZeusPlatformTenant tenant = this.tenantDetailsService.loadTenantById(tenantIdentifier);
        ThreadLocalContextUtil.setTenant(tenant);

        final AppUser appUser = event.getAppUser();
        final String authToken = event.getAuthToken();

        final HookEventSource hookEventSource = event.getSource();
        final String entityName = hookEventSource.getEntityName();
        final String actionName = hookEventSource.getActionName();
        final String payload = event.getPayload();

        final List<Hook> hooks = this.hookReadPlatformService.retrieveHooksByEvent(hookEventSource.getEntityName(),
                hookEventSource.getActionName());

        for (final Hook hook : hooks) {
            final HookProcessor processor = this.hookProcessorProvider.getProcessor(hook);
            try {
                processor.process(hook, appUser, payload, entityName, actionName, tenantIdentifier, authToken);
            } catch (Throwable e) {
                LOG.error("Hook {} failed in HookProcessor {} for tenantIdentifier/user {}/{}, entityName: {}, actionName: {}, payload {} ",
                        hook.getId(), processor.getClass().getSimpleName(), tenantIdentifier, appUser, entityName, actionName, payload, e);
            }
        }
    }
}
