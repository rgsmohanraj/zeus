package org.vcpl.lms.infrastructure.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "scheduler.alert.mail")
@Configuration
public class MailConfig {

    private String username;
    private String password;
    private String host;
    private String port;
    private Boolean useTLS;
    private Boolean isEnable;
    private String environment;
}
