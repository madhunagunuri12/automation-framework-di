package com.automation.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldReferenceObject(Object target, String fieldName) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    public static void setFieldReferenceObject(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field " + fieldName + " not found in " + clazz);
    }

    public static Object invokeMethod(Object target, String methodName) throws Exception {
        Method method = findMethod(target.getClass(), methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static Method findMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                // Assuming no arguments for now as getArguments() takes none
                return current.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException("Method " + methodName + " not found in " + clazz);
    }
}
