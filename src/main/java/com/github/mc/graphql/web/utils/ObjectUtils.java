package com.github.mc.graphql.web.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class ObjectUtils {

    /**
     * 为 null的数量
     * @return int
     */
    public static int nullCount(Object ... objs) {
        int count = 0;
        for (Object obj : objs) {
            if(obj == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 为 不为null的数量
     * @return int
     */
    public static int notNullCount(Object ... objs) {
        int count = 0;
        for (Object obj : objs) {
            if(obj != null) {
                count++;
            }
        }
        return count;
    }

    /**
     *  调用 set 方法
     * @param obj
     * @param aClass
     * @param field
     * @param val
     * @return
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static Object set(Object obj, Class<?> aClass, Field field, Object val) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = aClass.getDeclaredMethod("set"+firstToCapital(field.getName()), field.getType());
        return method.invoke(obj, val);
    }


    /**
     * get 获取对象属性值
     * @param obj
     * @param name
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Object get(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Class<?> aClass = obj.getClass();
        if(Map.class.isAssignableFrom(aClass)) {
            return ((Map)obj).get(name);
        }
        Field field = aClass.getDeclaredField(name);
        boolean accessible = field.isAccessible();
        if(!accessible) {
            field.setAccessible(true);
        }
        Object o = field.get(obj);
        if(!accessible) {
            field.setAccessible(false);
        }
        return o;

    }

    /**
     * 首字母大写
     * @param name
     * @return
     */
    public static String firstToCapital(String name) {
        return name.substring(0, 1).toUpperCase()+name.substring(1);
    }
}
