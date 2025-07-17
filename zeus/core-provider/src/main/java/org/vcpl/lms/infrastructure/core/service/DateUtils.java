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
package org.vcpl.lms.infrastructure.core.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.domain.ZeusPlatformTenant;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;

public final class DateUtils {

    private DateUtils() {

    }

    public static ZoneId getSystemZoneId() {
        return ZoneId.systemDefault();
    }

    public static ZoneId getDateTimeZoneOfTenant() {
        final ZeusPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        ZoneId zone = ZoneId.systemDefault();
        if (tenant != null) {
            zone = ZoneId.of(tenant.getTimezoneId());
        }
        return zone;
    }

    public static TimeZone getTimeZoneOfTenant() {
        final ZeusPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        TimeZone zone = null;
        if (tenant != null) {
            zone = TimeZone.getTimeZone(tenant.getTimezoneId());
        }
        return zone;
    }

    public static Date getDateOfTenant() {
        return convertLocalDateToDate(getLocalDateOfTenant());
    }

    public static Date convertLocalDateToDate(LocalDate localDate) {
        return createDate(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
    }

    public static Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    public static LocalDate getLocalDateOfTenant() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        return LocalDate.now(zone);
    }

    public static LocalDate getLocalDateOfTenant(final ZoneId zone) {
        return LocalDate.now(zone);
    }

    public static LocalDateTime getLocalDateTimeOfTenant() {
        final ZoneId zone = getDateTimeZoneOfTenant();
        LocalDateTime today = LocalDateTime.now(zone).truncatedTo(ChronoUnit.SECONDS);
        return today;
    }

    public static LocalDate parseLocalDate(final String stringDate, final String pattern) {

        try {
            final DateTimeFormatter dateStringFormat = DateTimeFormatter.ofPattern(pattern).withZone(getDateTimeZoneOfTenant());
            final ZonedDateTime dateTime = ZonedDateTime.parse(stringDate, dateStringFormat);
            return dateTime.toLocalDate();
        } catch (final IllegalArgumentException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.date.pattern",
                    "The parameter date (" + stringDate + ") is invalid w.r.t. pattern " + pattern, "date", stringDate, pattern);
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors, e);
        }
    }

    public static String formatToSqlDate(final Date date) {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(getTimeZoneOfTenant());
        final String formattedSqlDate = df.format(date);
        return formattedSqlDate;
    }

    public static boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(getLocalDateOfTenant());
    }
    public static String getCurrentMonthFirstDate() {
        return LocalDate.parse("2023-09-01").minusMonths(1)
                .with(TemporalAdjusters.firstDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getPreviousMonthFirstDate() {
        return LocalDate.parse("2023-09-01").minusMonths(2)
                .with(TemporalAdjusters.firstDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public static String getCurrentMonthLastDate() {
        return LocalDate.parse("2023-09-01").minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String getPreviousMonthLastDate() {
        return LocalDate.parse("2023-09-01").minusMonths(2)
                .with(TemporalAdjusters.lastDayOfMonth())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static LocalDate convertDateToLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
