package org.vcpl.lms.portfolio.loanproduct.domain;
import java.util.ArrayList;
import java.util.List;

public enum BrokenStrategyDayInYear {

        INVALID(0, "DaysInYearType.invalid"), //
        ACTUAL(1, "DaysInYearType.actual"), //
        NOBROKENDAYS(2,"DaysInYearType.nobrokendays"),
        DAYS_360(360, "DaysInYearType.days360"), //
        DAYS_364(364, "DaysInYearType.days364"), //
        DAYS_365(365, "DaysInYearType.days365");

        private final Integer value;
        private final String code;

    BrokenStrategyDayInYear(final Integer value, final String code) {
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
            for (final BrokenStrategyDayInYear enumType : values()) {
                if (enumType.getValue() > 0) {
                    values.add(enumType.getValue());
                }
            }

            return values.toArray();
        }

        public static BrokenStrategyDayInYear fromInt(final Integer type) {
            BrokenStrategyDayInYear brokenStrategyDayInYear = BrokenStrategyDayInYear.INVALID;
            if (type != null) {
                switch (type) {
                    case 1:
                        brokenStrategyDayInYear = BrokenStrategyDayInYear.ACTUAL;
                        break;
                    case 2:
                        brokenStrategyDayInYear = BrokenStrategyDayInYear.NOBROKENDAYS;
                        break;
                    case 360:
                        brokenStrategyDayInYear = BrokenStrategyDayInYear.DAYS_360;
                        break;
                    case 364:
                        brokenStrategyDayInYear =BrokenStrategyDayInYear.DAYS_364;
                        break;
                    case 365:
                        brokenStrategyDayInYear =BrokenStrategyDayInYear.DAYS_365;
                        break;
                }
            }
            return brokenStrategyDayInYear;
        }

        public boolean isActual() {
            return BrokenStrategyDayInYear.ACTUAL.getValue().equals(this.value);
        }

    public boolean is365() {
        return BrokenStrategyDayInYear.DAYS_365.getValue().equals(this.value);
    }

    public boolean is360() {
        return BrokenStrategyDayInYear.DAYS_360.getValue().equals(this.value);
    }
}


