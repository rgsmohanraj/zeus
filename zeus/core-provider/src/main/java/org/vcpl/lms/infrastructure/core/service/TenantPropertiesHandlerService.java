package org.vcpl.lms.infrastructure.core.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.vcpl.lms.infrastructure.core.config.TenantPropertiesHandler;
@Configuration
public class TenantPropertiesHandlerService {
    @Autowired
    private Environment environment;
    @Getter
    @Value("${multiple.tenant.configuration}")
    private String tenantsName;
    TenantPropertiesHandler tenantPropertiesHandler;
    public TenantPropertiesHandler getInstance() {
        return TenantPropertiesHandler.getInstance(this.tenantsName,this.environment);
    }
}