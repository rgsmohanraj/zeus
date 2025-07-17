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
package org.vcpl.lms.infrastructure.jobs.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.vcpl.lms.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.vcpl.lms.infrastructure.campaigns.email.service.EmailMessageJobEmailService;
import org.vcpl.lms.infrastructure.configuration.data.SMTPCredentialsData;
import org.vcpl.lms.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.vcpl.lms.infrastructure.core.config.MailConfig;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.jobs.domain.ScheduledJobDetail;
import org.vcpl.lms.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.vcpl.lms.useradministration.domain.AppUser;
import org.vcpl.lms.useradministration.domain.AppUserRepositoryWrapper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Global job Listener class to set Tenant details to {@link ThreadLocalContextUtil} for batch Job and stores the batch
 * job status to database after the execution
 *
 */
@Component
public class SchedulerJobListener implements JobListener {

    private int stackTraceLevel = 0;

    private final String name = SchedulerServiceConstants.DEFAULT_LISTENER_NAME;

    private final SchedularWritePlatformService schedularService;

    private final AppUserRepositoryWrapper userRepository;

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final EmailMessageJobEmailService emailMessageJobEmailService;

    private final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService;

    private final MailConfig mailConfig;




    @Autowired
    public SchedulerJobListener(final SchedularWritePlatformService schedularService, final AppUserRepositoryWrapper userRepository, EmailMessageJobEmailService emailMessageJobEmailService, ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService, MailConfig mailConfig) {
        this.schedularService = schedularService;
        this.userRepository = userRepository;
        this.emailMessageJobEmailService = emailMessageJobEmailService;
        this.externalServicesReadPlatformService = externalServicesReadPlatformService;
        this.mailConfig = mailConfig;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void jobToBeExecuted(@SuppressWarnings("unused") final JobExecutionContext context) {
        AppUser user = this.userRepository.fetchSystemUser();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
    public void jobExecutionVetoed(@SuppressWarnings("unused") final JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
        final Trigger trigger = context.getTrigger();
        final JobKey key = context.getJobDetail().getKey();
        final String jobKey = key.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + key.getGroup();
        final ScheduledJobDetail scheduledJobDetails = this.schedularService.findByJobKey(jobKey);
        final Long version = this.schedularService.fetchMaxVersionBy(jobKey) + 1;
        String status = SchedulerServiceConstants.STATUS_SUCCESS;
        String errorMessage = null;
        String errorLog = null;
        if (jobException != null) {
            status = SchedulerServiceConstants.STATUS_FAILED;
            this.stackTraceLevel = 0;
            final Throwable throwable = getCauseFromException(jobException);
            this.stackTraceLevel = 0;
            StackTraceElement[] stackTraceElements = null;
            errorMessage = throwable.getMessage();
            stackTraceElements = throwable.getStackTrace();
            final StringBuilder sb = new StringBuilder(throwable.toString());
            for (final StackTraceElement element : stackTraceElements) {
                sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                        .append(element.getLineNumber()).append(")");
            }
            errorLog = sb.toString();

        }
        String triggerType = SchedulerServiceConstants.TRIGGER_TYPE_CRON;
        if (context.getMergedJobDataMap().containsKey(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE)) {
            triggerType = context.getMergedJobDataMap().getString(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE);
        }
        if (SchedulerServiceConstants.TRIGGER_TYPE_CRON.equals(triggerType) && trigger.getNextFireTime() != null
                && trigger.getNextFireTime().after(scheduledJobDetails.getNextRunTime())) {
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
        }

        scheduledJobDetails.updatePreviousRunStartTime(context.getFireTime());
        scheduledJobDetails.updateCurrentlyRunningStatus(false);

        final ScheduledJobRunHistory runHistory = new ScheduledJobRunHistory(scheduledJobDetails, version, context.getFireTime(),
                new Date(), status, errorMessage, triggerType, errorLog);
        if (status.equals("failed") && mailConfig.getIsEnable()) {
            final SMTPCredentialsData smtpCredentialsData = this.externalServicesReadPlatformService.getSMTPCredentials();
            this.emailMessageJobEmailService.sendEmailWithAttachment(EmailMessageWithAttachmentData.createNew(null, generateBodyMessage(scheduledJobDetails), generateSubjectMessage(mailConfig.getEnvironment()), null, true),new SMTPCredentialsData(mailConfig.getUsername(), mailConfig.getPassword(), mailConfig.getHost(), mailConfig.getPort(), mailConfig.getUseTLS(),smtpCredentialsData.getFromEmail(),smtpCredentialsData.getFromName(),smtpCredentialsData.getToMail()));
        }
            // scheduledJobDetails.addRunHistory(runHistory);

        this.schedularService.saveOrUpdate(scheduledJobDetails, runHistory);

    }

    private Throwable getCauseFromException(final Throwable exception) {
        if (this.stackTraceLevel <= SchedulerServiceConstants.STACK_TRACE_LEVEL && exception.getCause() != null
                && (exception.getCause().toString().contains(SchedulerServiceConstants.SCHEDULER_EXCEPTION)
                        || exception.getCause().toString().contains(SchedulerServiceConstants.JOB_EXECUTION_EXCEPTION)
                        || exception.getCause().toString().contains(SchedulerServiceConstants.JOB_METHOD_INVOCATION_FAILED_EXCEPTION))) {
            this.stackTraceLevel++;
            return getCauseFromException(exception.getCause());
        } else if (exception.getCause() != null) {
            return exception.getCause();
        }
        return exception;
    }
    private String generateBodyMessage(ScheduledJobDetail scheduledJobDetail) {
        return "Hi Team ," + "<br>" + "<br>" + "This Mail is to inform that <b>" + scheduledJobDetail.getJobName() + "</b> job has been failed." + "<br>" + "<br>" +
                " NOTE : This is an automated message. Please do not reply." + "<br>" + "<br>Thank You<br>";
    }

    private String generateSubjectMessage(String environment) {
        return "[Alert: "+environment.toUpperCase()+"] Zeus Job has been failed";
    }

}
