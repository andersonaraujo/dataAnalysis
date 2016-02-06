package org.andersonaraujo.dataAnalysis.test.util;

import java.lang.reflect.Field;

public class TestUtil {

    /**
     * Get a field from an object. This method can be used to get a field that
     * is normally not accessible, like private, or protected field.
     *
     * @param obj       the object.
     * @param fieldName the field to be get.
     * @return value of field
     * @throws Exception if the method does not exist for the object.
     */
    public static <T> T getFieldOnObject(Object obj, String fieldName, Class<T> clazz) throws Exception {
        return getFieldOnObject(obj.getClass(), obj, fieldName, clazz);
    }

    /**
     * Get a field from an object. This method can be used to get a field that
     * is normally not accessible, like private, or protected field.
     *
     * @param cls       the class that declares the field.
     * @param obj       the object.
     * @param fieldName the field to be get.
     * @return value of field
     * @throws Exception if the method does not exist for the object.
     */
    public static <T> T getFieldOnObject(Class<?> cls, Object obj, String fieldName, Class<T> clazz) throws Exception {
        try {
            final Field theField = cls.getDeclaredField(fieldName);
            theField.setAccessible(true);
            return clazz.cast(theField.get(obj));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Field " + fieldName + " does"
                    + " not exist.");
        }
    }

}
