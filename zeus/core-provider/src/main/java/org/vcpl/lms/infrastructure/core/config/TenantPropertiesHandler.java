package org.vcpl.lms.infrastructure.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenantConnection;
import org.vcpl.lms.infrastructure.security.exception.InvalidTenantIdentiferException;

import java.lang.reflect.Field;
import java.util.*;
public class TenantPropertiesHandler {
    private static TenantPropertiesHandler singletonScopeInstance;
    @Getter
    Map<String, ZeusPlatformTenant> tenantPlatformDetails = new HashMap<>(1);
    private TenantPropertiesHandler(){}
    public static TenantPropertiesHandler getInstance(String tenantsName,Environment environment) {
        if (Objects.isNull(singletonScopeInstance)) {
            singletonScopeInstance = new TenantPropertiesHandler();
            singletonScopeInstance.addTenantProperties(tenantsName,environment);
        }
        return singletonScopeInstance;
    }

    private void addTenantProperties(String tenantsName,Environment environment) {
        if (StringUtils.isEmpty(tenantsName)) {
            throw new InvalidTenantIdentiferException("Unable to get the current tentants Name");
        }
        for (String tenantName : tenantsName.split(",")) {
            this.tenantPlatformDetails.put(tenantName, getTenantSpecificData(tenantName, environment));
        }
    }

    private ZeusPlatformTenant getTenantSpecificData(String tenantName, Environment environment) {
        String keyData = "tenant.configuration.".concat(tenantName).concat(".");
        Field[] fieldsTenantConnection = ZeusPlatformTenantConnection.class.getDeclaredFields();
        Field[] fieldsTenant = ZeusPlatformTenant.class.getDeclaredFields();
        Map<String, Object> properties = new HashMap<>();
        for (Field field : fieldsTenantConnection) {
            String propValue = environment.getProperty(keyData.concat("connection.").concat(field.getName()));
            if (StringUtils.isNotEmpty(propValue)) {
                properties.put(field.getName(), propValue);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        if(properties.isEmpty())
            return null;
        ZeusPlatformTenantConnection tenantConnectionInstance = objectMapper.convertValue(properties, ZeusPlatformTenantConnection.class);
        properties.clear();
        for (Field field : fieldsTenant) {
            String propValue = environment.getProperty(keyData.concat(field.getName()));
            if(StringUtils.isNotEmpty(propValue)){
                properties.put(field.getName(),propValue);
            }
        }
        if(properties.isEmpty())
            return null;
        ZeusPlatformTenant tenantInstance = objectMapper.convertValue(properties, ZeusPlatformTenant.class);
        tenantInstance.setConnection(tenantConnectionInstance);
        return tenantInstance;
    }
}