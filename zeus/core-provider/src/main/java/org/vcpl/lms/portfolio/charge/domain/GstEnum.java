package org.vcpl.lms.portfolio.charge.domain;


public enum GstEnum {
        INVALID(0,"invaild" ),
        INCLUSIVE(1,"GstEnum.inclusive"),
        EXCLUSIVE(2,"GstEnum.exclusive");


        private final Integer value;
        private final String code;



        GstEnum(final Integer value, final String code) {
            this.value = value;
            this.code = code;
        }
        public Integer getValue() {
            return value;
        }

        public String getCode() {
            return code;
        }
        public static GstEnum fromInt(final Integer selectedValue) {

            GstEnum gst = null;
            switch (selectedValue) {
                case 1:
                    gst = GstEnum.INCLUSIVE;
                    break;
                case 2:
                    gst = GstEnum.EXCLUSIVE;
                    break;

                default:
                    gst = GstEnum.INVALID;
                    break;
            }
            return gst;
        }

        public static Boolean isInclusive(GstEnum gstEnum)
        {
            return gstEnum.value.equals(1);

        }

}

