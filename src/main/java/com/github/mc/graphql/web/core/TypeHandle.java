package com.github.mc.graphql.web.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public interface TypeHandle {

    String javaToGraphqlType(Class<?> type);

    boolean isNeedDefineType(Class<?> type);

    boolean isGraphqlArray(Class<?> type);

    Class<?> graphqlArrayJavaType(Field field);

    Class<?> graphqlArrayJavaType(Type genericType);
}
