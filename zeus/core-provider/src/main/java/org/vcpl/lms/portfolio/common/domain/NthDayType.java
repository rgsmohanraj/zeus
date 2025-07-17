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
package org.vcpl.lms.portfolio.common.domain;

public enum NthDayType {

    ONE(1, "nthDayType.1"), TWO(2, "nthDayType.2"), THREE(3, "nthDayType.3"), FOUR(4, "nthDayType.4"), FIVE(5, "nthDayType.5"), SIX(6, "nthDayType.6"),

    SEVEN(7, "nthDayType.7"),EIGHT(8,"nthDayType.8"),NINE(9,"nthDayType.9"),TEN(10,"nthDayType.10"),ELEVEN(11,"nthDayType.eleven"),

    TWELVE(12, "nthDayType.12"),THIRTEEN (13, "nthDayType.13"),FOURTEEN (14, "nthDayType.14"), FIFTEEN(15, "nthDayType.15"),

    SIXTEEN(16, "nthDayType.16"), SEVENTEEN(17, "nthDayType.17"), EIGHTEEN(18, "nthDayType.18"), NINETEEN(19, "nthDayType.nineteen"),

    TWENTY(20, "nthDayType.20"), TWENTYONE(21, "nthDayType.21"),TWENTYTWO(22,"nthDayType.22"),TWENTYTHREE(23,"nthDayType.23"),

    TWENTYFOUR(24,"nthDayType.24"),TWENTYFIVE(25,"nthDayType.25"),TWENTYSIX(26, "nthDayType.26"), TWENTYSEVEN(27, "nthDayType.27"),

    TWENTYEIGHT(28, "nthDayType.28"), TWENTYNINE(29, "nthDayType.29"), THIRTY(30,"nthDayType.30"),THIRTYONE(31, "nthDayType.31"),LAST(-1, "nthDayType.last"), ONDAY(-2, "nthDayType.onday"), INVALID(0, "nthDayType.invalid"),;
    private final Integer value;
    private final String code;

    NthDayType(Integer value, String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static NthDayType fromInt(final Integer frequency) {
        NthDayType repaymentFrequencyNthDayType = NthDayType.INVALID;
        if (frequency != null) {
            switch (frequency) {

                case 1:

                    repaymentFrequencyNthDayType = NthDayType.ONE;

                    break;

                case 2:

                    repaymentFrequencyNthDayType = NthDayType.TWO;

                    break;

                case 3:

                    repaymentFrequencyNthDayType = NthDayType.THREE;

                    break;

                case 4:

                    repaymentFrequencyNthDayType = NthDayType.FOUR;

                    break;

                case 5:

                    repaymentFrequencyNthDayType = NthDayType.FIVE;

                    break;

                case 6:

                    repaymentFrequencyNthDayType = NthDayType.SIX;

                    break;

                case 7:

                    repaymentFrequencyNthDayType = NthDayType.SEVEN;

                    break;

                case 8:

                    repaymentFrequencyNthDayType = NthDayType.EIGHT;

                    break;

                case 9:

                    repaymentFrequencyNthDayType = NthDayType.NINE;

                    break;

                case 10:

                    repaymentFrequencyNthDayType = NthDayType.TEN;

                    break;

                case 11:

                    repaymentFrequencyNthDayType = NthDayType.ELEVEN;

                    break;

                case 12:

                    repaymentFrequencyNthDayType = NthDayType.TWELVE;

                    break;

                case 13:

                    repaymentFrequencyNthDayType = NthDayType.THIRTEEN;

                    break;

                case 14:

                    repaymentFrequencyNthDayType = NthDayType.FOURTEEN;

                    break;

                case 15:

                    repaymentFrequencyNthDayType = NthDayType.FIFTEEN;

                    break;

                case 16:

                    repaymentFrequencyNthDayType = NthDayType.SIXTEEN;

                    break;

                case 17:

                    repaymentFrequencyNthDayType = NthDayType.SEVENTEEN;

                    break;

                case 18:

                    repaymentFrequencyNthDayType = NthDayType.EIGHTEEN;

                    break;

                case 19:

                    repaymentFrequencyNthDayType = NthDayType.NINETEEN;

                    break;

                case 20:

                    repaymentFrequencyNthDayType = NthDayType.TWENTY;

                    break;

                case 21:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYONE;

                    break;

                case 22:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYTWO;

                    break;

                case 23:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYTHREE;

                    break;

                case 24:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYFOUR;

                    break;

                case 25:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYFIVE;

                    break;

                case 26:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYSIX;

                    break;

                case 27:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYSEVEN;

                    break;

                case 28:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYEIGHT;

                    break;

                case 29:

                    repaymentFrequencyNthDayType = NthDayType.TWENTYNINE;

                    break;

                case 30:

                    repaymentFrequencyNthDayType = NthDayType.THIRTY;

                    break;

                case 31:

                    repaymentFrequencyNthDayType = NthDayType.THIRTYONE;

                    break;

                case -1:

                    repaymentFrequencyNthDayType = NthDayType.LAST;

                    break;

                case -2:

                    repaymentFrequencyNthDayType = NthDayType.ONDAY;

                    break;

                default:

                    break;

            }

        }

        return repaymentFrequencyNthDayType;
    }

    public boolean isInvalid() {
        return this.value.equals(NthDayType.INVALID.value);
    }

    public boolean isLastDay() {
        return this.value.equals(NthDayType.LAST.value);
    }

    public boolean isOnDay() {
        return this.value.equals(NthDayType.ONDAY.value);
    }
}
