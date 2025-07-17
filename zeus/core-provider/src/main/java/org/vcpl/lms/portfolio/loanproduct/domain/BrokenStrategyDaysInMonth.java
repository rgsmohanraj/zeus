package org.vcpl.lms.portfolio.loanproduct.domain;


import java.util.ArrayList;
import java.util.List;

public enum BrokenStrategyDaysInMonth {


        INVALID(0, "DaysInMonthType.invalid"), //
        ACTUAL(1, "DaysInMonthType.actual"), //
        DAYS_30(30, "DaysInMonthType.days360"),
        DAYS_31(31, "DaysInMonthType.days365");//


        private final Integer value;
        private final String code;

    BrokenStrategyDaysInMonth(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }

        public Integer getValue() {
            return this.value;
        }

        public String getCode() {
            return this.code;
        }

        public static Object[] integerValues() {
            final List<Integer> values = new ArrayList<>();
            for (final BrokenStrategyDaysInMonth enumType : values()) {
                if (enumType.getValue() > 0) {
                    values.add(enumType.getValue());
                }
            }

            return values.toArray();
        }

        public static BrokenStrategyDaysInMonth fromInt(final Integer type) {
            BrokenStrategyDaysInMonth repaymentFrequencyType = BrokenStrategyDaysInMonth.INVALID;
            if (type != null) {
                switch (type) {
                    case 1:
                        repaymentFrequencyType = BrokenStrategyDaysInMonth.ACTUAL;
                        break;
                    case 30:
                        repaymentFrequencyType = BrokenStrategyDaysInMonth.DAYS_30;
                        break;
                    case 31:
                        repaymentFrequencyType = BrokenStrategyDaysInMonth.DAYS_31;
                        break;
                }
            }
            return repaymentFrequencyType;
        }





    }
