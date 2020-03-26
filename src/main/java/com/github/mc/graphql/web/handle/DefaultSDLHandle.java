package com.github.mc.graphql.web.handle;

import com.github.mc.graphql.web.GraphQLProperties;
import com.github.mc.graphql.web.annotations.*;
import com.github.mc.graphql.web.core.MapperHandle;
import com.github.mc.graphql.web.core.SDLHandle;
import com.github.mc.graphql.web.core.TypeHandle;
import graphql.schema.GraphQLScalarType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DefaultSDLHandle implements SDLHandle {
    @Autowired
    private TypeHandle typeHandle;
    @Autowired
    private GraphQLProperties graphQLProperties;
    @Autowired
    private List<GraphQLScalarType> scalarTypes;

    private boolean getRan(Object keyAnnotation) {
        if(keyAnnotation instanceof Query) {
            return ((Query) keyAnnotation).ran();
        }else if(keyAnnotation instanceof Mutation) {
            return ((Mutation) keyAnnotation).ran();
        }else {
            return ((Subscription) keyAnnotation).ran();
        }
    }

    @Override
    public String sdl(List<MapperHandle.Mapper> mappers) {

        StringBuilder query = new StringBuilder("type Query {\n");
        StringBuilder mutation = new StringBuilder("type Mutation {\n");
        StringBuilder subscription = new StringBuilder("type Subscription {\n");
        LinkedHashSet<Class<?>> typeSet = new LinkedHashSet<>();

        for (MapperHandle.Mapper mapper : mappers) {
            if(mapper.keyAnnotation instanceof Query) {
                query.append(sdlMethod(mapper, ((Query) mapper.keyAnnotation).rtn(), typeSet));
            }else if(mapper.keyAnnotation instanceof Mutation) {
                mutation.append(sdlMethod(mapper, ((Mutation) mapper.keyAnnotation).rtn(), typeSet));
            }else if(mapper.keyAnnotation instanceof Subscription) {
                subscription.append(sdlMethod(mapper, ((Subscription) mapper.keyAnnotation).rtn(), typeSet));
            }
        }

        query.append("}\n\n");
        mutation.append("}\n\n");
        subscription.append("}\n\n");
        return String.valueOf(query) +
                mutation +
                subscription +
                sdlType(typeSet);
    }

    @Override
    public String sdlMethod(MapperHandle.Mapper mapper, boolean returnNotNull, Set<Class<?>> typeSet) {
        StringBuilder sb = new StringBuilder("  ")
                .append(mapper.method.getName());
        if(mapper.mapping != null) {
            sb.append(mapper.mapping.value());
        }
        Class<?> returnType = mapper.method.getReturnType();

        sb.append(sdlParameter(filterParameter(mapper.parameters), typeSet))
                .append(": ");
        boolean graphqlArray = typeHandle.isGraphqlArray(returnType);
        if(graphqlArray) {
            sb.append("[");
            returnType = typeHandle.graphqlArrayJavaType(mapper.method.getGenericReturnType());
        }
        sb.append(typeHandle.javaToGraphqlType(returnType));

        if(typeHandle.isNeedDefineType(returnType)) {
            typeSet.add(returnType);
        }
        if(returnNotNull) {
            sb.append("!");
        }
        if(graphqlArray) {
            sb.append("]");
            if(getRan(mapper.keyAnnotation)) {
                sb.append("!");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String sdlParameter(List<Parameter> parameters, Set<Class<?>> typeSet) {
        if(parameters == null || parameters.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("(");
        String name;
        boolean notNull;
        boolean graphqlArray;
        Class<?> type;
        for (Parameter parameter : parameters) {
            name = parameter.getName();
            notNull = parameterNotNull(parameter);
            type = parameter.getType();
            graphqlArray = typeHandle.isGraphqlArray(type);
            if(name.equals(graphQLProperties.getIdField())) {
                sb.append(graphQLProperties.getIdField());
                if(graphqlArray) {
                    sb.append(": [ID");
                }else {
                    sb.append(": ID");
                }
                if(notNull) {
                    sb.append("!");
                }
                if(graphqlArray) {
                    sb.append("]");
                    if(arrayNotNull(parameter)) {
                        sb.append("!");
                    }
                }
                sb.append(",");
            }else {
                sb.append(name)
                        .append(": ");
                if(graphqlArray) {
                    type = typeHandle.graphqlArrayJavaType(parameter.getParameterizedType());
                    sb.append("[");
                }
                sb.append(typeHandle.javaToGraphqlType(type));
                if(typeHandle.isNeedDefineType(type)) {
                    typeSet.add(type);
                }
                if(notNull) {
                    sb.append("!");
                }
                if(graphqlArray) {
                    sb.append("]");
                    if(arrayNotNull(parameter)) {
                        sb.append("!");
                    }
                }
                sb.append(", ");
            }

        }
        sb.setLength(sb.length() - 2);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean parameterNotNull(Parameter parameter) {
        return notNull(parameter);
    }

    @Override
    public boolean fieldNotNull(Field field) {
        return notNull(field);
    }

    public boolean notNull(AnnotatedElement element) {
        Annotation[] annotations = element.getAnnotations();
        for (Annotation annotation : annotations) {
            if(NotNull.class.getName().equals(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String sdlType(LinkedHashSet<Class<?>> typeSet) {
        StringBuilder sb = new StringBuilder();

        for (GraphQLScalarType scalarType : scalarTypes) {
            sb.append("scalar ");
            sb.append(scalarType.getName());
            sb.append("\n");
        }
        sb.append("\n");

        if(typeSet == null || typeSet.isEmpty()) {
            return sb.toString();
        }
        ArrayList<Class<?>> list = new ArrayList<>(typeSet);
        int size = list.size();
        Class<?> type;
        List<Field> fields;
        for (int i = 0; i < size; i++) {
            type = list.get(i);
            sb.append("type ")
                    .append(type.getSimpleName())
                    .append(" {\n");

            fields = filterFields(type.getDeclaredFields());

            for (Field field : fields) {
                sb.append("  ")
                        .append(field.getName())
                        .append(": ");
                boolean notNull = fieldNotNull(field);
                Class<?> fieldType = field.getType();
                boolean graphqlArray = typeHandle.isGraphqlArray(fieldType);
                if(field.getName().equals(graphQLProperties.getIdField())) {
                    if(graphqlArray) {
                        sb.append("[");
                    }
                    if(notNull) {
                        sb.append("ID!");
                    }else {
                        sb.append("ID");
                    }
                    if(graphqlArray) {
                        sb.append("]");
                        if(arrayNotNull(field)) {
                            sb.append("!");
                        }
                    }
                    sb.append("\n");
                }else {
                    if(graphqlArray) {
                        sb.append("[");
                        fieldType = typeHandle.graphqlArrayJavaType(field);
                    }

                    sb.append(typeHandle.javaToGraphqlType(fieldType));
                    if(typeHandle.isNeedDefineType(fieldType)) {
                        if(!list.contains(fieldType)) {
                            list.add(fieldType);
                        }
                    }
                    if(notNull) {
                        sb.append("!");
                    }
                    if(graphqlArray) {
                        sb.append("]");
                        if(arrayNotNull(field)) {
                            sb.append("!");
                        }
                    }
                    sb.append("\n");
                }
            }
            sb.append("}\n\n");
            size = list.size();
        }
        return sb.toString();
    }

    @Override
    public List<Parameter> filterParameter(Parameter[] parameters) {
        List<Parameter> list = new ArrayList<>();
        Class<?> type;
        Ignore ignore;
        Attribute attribute;
        for (Parameter parameter : parameters) {
            type = parameter.getType();
            if(ServletRequest.class.isAssignableFrom(type) || ServletResponse.class.isAssignableFrom(type)) {
                continue;
            }
            ignore = parameter.getAnnotation(Ignore.class);
            attribute = parameter.getAnnotation(Attribute.class);
            if(ignore != null || attribute != null) {
                continue;
            }
            list.add(parameter);
        }
        return list;
    }

    @Override
    public List<Field> filterFields(Field[] fields) {
        List<Field> list = new ArrayList<>();
        Ignore ignore;
        for (Field field : fields) {
            ignore = field.getAnnotation(Ignore.class);
            if(ignore != null) {
                continue;
            }
            list.add(field);
        }
        return list;
    }

    @Override
    public boolean arrayNotNull(AnnotatedElement element) {
        ArrayNotNull annotation = element.getAnnotation(ArrayNotNull.class);
        return annotation != null;
    }
}
