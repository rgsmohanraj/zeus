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
package org.vcpl.lms.infrastructure.security.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.jfree.util.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
import org.vcpl.lms.infrastructure.security.domain.PlatformUser;
import org.vcpl.lms.infrastructure.security.domain.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.vcpl.lms.organisation.office.domain.Office;
import org.vcpl.lms.organisation.office.domain.OfficeRepository;
import org.vcpl.lms.spm.domain.Response;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepository;
import org.vcpl.lms.useradministration.domain.Role;
import org.vcpl.lms.useradministration.domain.RoleRepository;
import org.vcpl.lms.useradministration.exception.RoleNotFoundException;
import org.vcpl.lms.useradministration.exception.UserNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Used in securityContext.xml as implementation of spring security's {@link UserDetailsService}.
 */
@Service("userDetailsService")
@Slf4j
public class TenantAwareJpaPlatformUserDetailsService implements PlatformUserDetailsService {

    @Autowired
    private PlatformUserRepository platformUserRepository;
    private final AppUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final OfficeRepository officeRepository;

    private final AppUserRepository appUserRepository;

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Value("${lms.security.oauth.enabled}")
    private Boolean oauthEnabled;

    public TenantAwareJpaPlatformUserDetailsService(PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService, AppUserRepository userRepository, RoleRepository roleRepository, OfficeRepository officeRepository, AppUserRepository appUserRepository) {
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.officeRepository = officeRepository;
        this.appUserRepository = appUserRepository;
    }


    @Override
    @Cacheable(value = "usersByUsername", key = "T(org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#username+'ubu')")
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {

        // Retrieve active users only
        final boolean deleted = false;
        final boolean enabled = true;
        return this.platformUserRepository.findByUsernameAndDeletedAndEnabled(username, deleted, enabled);
    }

    public UserDetails loadUserByUsernameOauth(final String username, Collection<GrantedAuthority> authorityCollection, List<String> keyCloakRoles) throws Exception {
        final boolean deleted = false;
        final boolean enabled = true;
        final PlatformUser appUser = this.platformUserRepository.findByUsernameAndDeletedAndEnabled(username, deleted, enabled);
        if (keyCloakRoles.isEmpty()) {
            Log.error("Role Not Mapped in Keycloak");
            throw new Exception("Role Not Mapped in Keycloak");
        }
        if ((oauthEnabled && appUser == null)) {
            Set<Role> roleSet = roleRepository.getRoleByNames(keyCloakRoles);
            Optional<Office> office = officeRepository.findById(1L);
            User user = new User(username, "", true, true, true, true, authorityCollection);
            AppUser appUserData = new AppUser(office.get(), user, roleSet, username + "@gmail.com", username, username, null, false, false, null, false);
            userRepository.save(appUserData);
        }
        final AppUser userToUpdate = this.appUserRepository.findAppUserByName(username);
        Set<Role> roleSet = roleRepository.getRoleByNames(keyCloakRoles);
        if (compare(roleSet, userToUpdate.getRoles())) {
            userToUpdate.updateRoles(roleSet);
            userRepository.save(userToUpdate);
        }
        return this.platformUserRepository.findByUsernameAndDeletedAndEnabled(username, deleted, enabled);
    }

    public boolean compare(Set<Role> firstSet, Set<Role> secondSet) {
        int flag = 0;
        if (firstSet.size() != secondSet.size()) {
            return true;
        }
        for (Role firstRecord : firstSet) {
            flag = 0;
            for (Role secondRecord : secondSet) {
                if (firstRecord.getName().equals(secondRecord.getName())) {
                    flag = 1;
                }
            }
            if (flag == 0) {
                return true;
            }
        }
        return false;
    }
}
