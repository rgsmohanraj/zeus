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
package org.vcpl.lms;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.vcpl.lms.infrastructure.core.boot.AbstractApplicationConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.vcpl.lms.portfolio.client.utils.AESEncryptionUtils;

/**
 * Fineract main() application which launches Fineract in an embedded Tomcat HTTP (using Spring Boot).
 *
 * The DataSource used is a to a "normal" external database (not use MariaDB4j). This DataSource can be configured with
 * parameters, see {@link DataSourceProperties}.
 *
 * You can easily launch this via Debug as Java Application in your IDE - without needing command line Gradle stuff, no
 * need to build and deploy a WAR, remote attachment etc.
 *
 * It's the old/classic Mifos (non-X) Workspace 2.0 reborn for Fineract! ;-)
 *
 */

@SpringBootApplication
@EnableAutoConfiguration
public class ServerApplication extends SpringBootServletInitializer {

    @Value("${zeus.encryption.iv}")
    public void setIVParameter(String ivParameter){
        AESEncryptionUtils.IV_PARAMETER = ivParameter;
    }

    @Value("${zeus.encryption.password}")
    public void setPassword(String password){
        AESEncryptionUtils.PASSWORD = password;
    }

    @Value("${zeus.encryption.salt}")
    public void setSalt(String salt){
        AESEncryptionUtils.SALT = salt;
    }
    @Value("${zeus.encryption.secretkey}")
    public void setSecretKey(String secretKey){ AESEncryptionUtils.PRIVATE_KEY=secretKey; }

    private static class Configuration extends AbstractApplicationConfiguration {}
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(Configuration.class);
    }

    private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
        return builder.sources(Configuration.class);
    }

    public static void main(String[] args) throws IOException {
        configureApplication(new SpringApplicationBuilder(ServerApplication.class)).run(args);
        // ApplicationExitUtil.waitForKeyPressToCleanlyExit(ctx);
    }
}
