package org.vcpl.lms.portfolio.loanaccount.bulkupload.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class CommonUtils {
    public static boolean getNonNullBoolean(Map<String, Object> mapDetails, String getKeyName) {
        return Optional.ofNullable(mapDetails).map(data -> mapDetails.get(getKeyName)).map(Boolean.class::cast).orElse(Boolean.FALSE);
    }

    public static <T> T getNonNullData(Map<String,Object> mapDetails, String getKeyName){
        return Optional.ofNullable(mapDetails).map(data -> mapDetails.get(getKeyName)).map(data -> (T) data).orElse(null);
    }

    public static Field getFieldValueFromObject(Object o, String k) throws NoSuchFieldException {
        Field field = o.getClass().getDeclaredField(k);
        field.setAccessible(Boolean.TRUE);
        return field;
    }
}
