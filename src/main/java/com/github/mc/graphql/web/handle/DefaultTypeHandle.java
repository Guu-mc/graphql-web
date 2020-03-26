package com.github.mc.graphql.web.handle;

import com.github.mc.graphql.web.core.TypeHandle;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;

@Component
public class DefaultTypeHandle implements TypeHandle {
    @Override
    public String javaToGraphqlType(Class<?> type) {
        String graphqlType;
        String name = type.getName();
        if("byte".equals(name) || Byte.class.getName().equals(name)) {
            graphqlType = "String";
        }else if("short".equals(name) || Short.class.getName().equals(name)) {
            graphqlType = "Short";
        }else if("int".equals(name) || Integer.class.getName().equals(name)) {
            graphqlType = "Int";
        }else if("long".equals(name) || Long.class.getName().equals(name)) {
            graphqlType = "Long";
        }else if("float".equals(name) || Float.class.getName().equals(name)) {
            graphqlType = "Float";
        }else if("double".equals(name) || Double.class.getName().equals(name)) {
            graphqlType = "Float";
        }else if("boolean".equals(name) || Boolean.class.getName().equals(name)) {
            graphqlType = "Boolean";
        }else if("char".equals(name) || Character.class.getName().equals(name)) {
            graphqlType = "Char";
        }else {
            graphqlType = type.getSimpleName();
        }
        return graphqlType;
    }

    @Override
    public boolean isNeedDefineType(Class<?> type) {
        String name = type.getName();
        return !"byte".equals(name) && !Byte.class.getName().equals(name) &&
                !"short".equals(name) && !Short.class.getName().equals(name) &&
                !"int".equals(name) && !Integer.class.getName().equals(name) &&
                !"long".equals(name) && !Long.class.getName().equals(name) &&
                !"float".equals(name) && !Float.class.getName().equals(name) &&
                !"double".equals(name) && !Double.class.getName().equals(name) &&
                !"boolean".equals(name) && !Boolean.class.getName().equals(name) &&
                !"char".equals(name) && !Character.class.getName().equals(name) &&
                !String.class.getName().equals(name) && !Date.class.getName().equals(name);
    }

    @Override
    public boolean isGraphqlArray(Class<?> type) {
        return type.isArray() || Collection.class.isAssignableFrom(type);
    }

    @Override
    public Class<?> graphqlArrayJavaType(Field field) {
        Class<?> type = field.getType();
        if(type.isArray()) { //处理数组
            type = type.getComponentType();
        }else if(Collection.class.isAssignableFrom(field.getType())) { //处理容器泛型
            Type genericType = field.getGenericType();
            if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
                Type actualTypeArgument = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                String typeName = actualTypeArgument.getTypeName();
                type = ClassForName(typeName);
            }
        }
        return type;
    }

    @Override
    public Class<?> graphqlArrayJavaType(Type genericType) {
        String typeName = genericType.getTypeName();
        if(typeName.endsWith("[]")) {
            typeName = typeName.substring(0, typeName.length() - 2);
        }else if (ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
            Type actualTypeArgument = ((ParameterizedType) genericType).getActualTypeArguments()[0];
            typeName = actualTypeArgument.getTypeName();
        }
        return ClassForName(typeName);
    }

    public Class<?> ClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to get class: "+className);
        }
    }
}
