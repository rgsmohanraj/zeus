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
package org.vcpl.lms.notification.eventandlistener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.vcpl.lms.notification.data.NotificationData;
import org.vcpl.lms.notification.service.NotificationWritePlatformService;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationEventListener implements SessionAwareMessageListener {

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;

    private final NotificationWritePlatformService notificationWritePlatformService;

    private final AppUserRepository appUserRepository;

    @Autowired
    public NotificationEventListener(BasicAuthTenantDetailsService basicAuthTenantDetailsService,
            NotificationWritePlatformService notificationWritePlatformService, AppUserRepository appUserRepository) {
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
        this.notificationWritePlatformService = notificationWritePlatformService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        if (message instanceof ObjectMessage) {
            NotificationData notificationData = (NotificationData) ((ObjectMessage) message).getObject();

            final ZeusPlatformTenant tenant = this.basicAuthTenantDetailsService.loadTenantById(notificationData.getTenantIdentifier(),
                    false);
            ThreadLocalContextUtil.setTenant(tenant);

            Long appUserId = notificationData.getActor();

            List<Long> userIds = notificationData.getUserIds();

            if (notificationData.getOfficeId() != null) {
                List<Long> tempUserIds = new ArrayList<>(userIds);
                for (Long userId : tempUserIds) {
                    AppUser appUser = appUserRepository.findById(userId).get();
                    if (!Objects.equals(appUser.getOffice().getId(), notificationData.getOfficeId())) {
                        userIds.remove(userId);
                    }
                }
            }

            if (userIds.contains(appUserId)) {
                userIds.remove(appUserId);
            }

            notificationWritePlatformService.notify(userIds, notificationData.getObjectType(), notificationData.getObjectIdentfier(),
                    notificationData.getAction(), notificationData.getActor(), notificationData.getContent(),
                    notificationData.isSystemGenerated());
        }
    }
}
