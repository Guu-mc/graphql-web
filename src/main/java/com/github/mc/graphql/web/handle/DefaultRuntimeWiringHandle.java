package com.github.mc.graphql.web.handle;

import com.github.mc.graphql.web.HTTPRoot;
import com.github.mc.graphql.web.annotations.*;
import com.github.mc.graphql.web.core.MapperHandle;
import com.github.mc.graphql.web.core.RuntimeWiringHandle;
import com.github.mc.graphql.web.core.TypeHandle;
import com.github.mc.graphql.web.utils.ObjectUtils;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.List;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Component
public class DefaultRuntimeWiringHandle implements RuntimeWiringHandle, ApplicationContextAware {

    protected ApplicationContext context;

    @Autowired
    private TypeHandle typeHandle;
    @Autowired
    private List<GraphQLScalarType> scalarTypes;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public RuntimeWiring runtimeWiring(List<MapperHandle.Mapper> mappers) {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();

        for (GraphQLScalarType scalarType : scalarTypes) {
            builder.scalar(scalarType);
        }
        primary(mappers, builder);
        return builder.build();
    }


    public void primary(List<MapperHandle.Mapper> mappers, RuntimeWiring.Builder builder) {
        for (MapperHandle.Mapper mapper : mappers) {
            TypeRuntimeWiring.Builder t;
            if(mapper.keyAnnotation instanceof Query) {
                t = newTypeWiring("Query");
            }else if(mapper.keyAnnotation instanceof Mutation) {
                t = newTypeWiring("Mutation");
            }else if(mapper.keyAnnotation instanceof Subscription) {
                t = newTypeWiring("Subscription");
            }else {
                if(mapper.mapping == null) {
                    throw new RuntimeException("The Mapping definition was not found in "+mapper.aClass.getName());
                }
                t = newTypeWiring(mapper.mapping.value());
            }
            String name = mapper.method.getName();
            if(mapper.mapping != null && !(mapper.keyAnnotation instanceof Type)) {
                name += mapper.mapping.value();
            }
            t.dataFetcher(name, dataFetchingEnvironment -> {
                Object bean = context.getBean(mapper.aClass);
                Object[] args = new Object[mapper.parameters.length];
                Object source = dataFetchingEnvironment.getSource();
                Parameter parameter;
                Class<?> type;
                HTTPRoot root;
                Object o;
                for (int i = 0; i < args.length; i++) {
                    parameter = mapper.parameters[i];
                    type = parameter.getType();
                    root = dataFetchingEnvironment.getRoot();

                    ServletRequest request = root.getRequest();
                    if(ServletRequest.class.isAssignableFrom(type)) {
                        args[i] = root.getRequest();
                    }else if(ServletResponse.class.isAssignableFrom(type)) {
                        args[i] = root.getResponse();
                    }else {
                        Attribute attribute = parameter.getAnnotation(Attribute.class);
                        if(attribute != null) {
                            String value = attribute.value();
                            if(value.isEmpty()) {
                                o = request.getAttribute(parameter.getName());
                            }else {
                                o = request.getAttribute(value);
                            }
                            oNotNull(mapper, parameter, o);
                        }else if(mapper.keyAnnotation instanceof Type) {
                            o = ObjectUtils.get(source, parameter.getName());
                            oNotNull(mapper, parameter, o);
                            args[i] = o;
                        }else {
                            o = dataFetchingEnvironment.getArgument(parameter.getName());
                        }
                        args[i] = o;
                    }
                }
                Object res;
                try {
                    res = mapper.method.invoke(bean, args);
                    resultFilter(res);
                } catch(Exception e) {
                    throw (Exception)e.getCause();
                }
                return res;
            });
            builder.type(t);
        }
    }

    public void oNotNull(MapperHandle.Mapper mapper, Parameter parameter, Object o) {
        if(notNull(parameter)) {
            if(o == null) {
                throw new RuntimeException(parameter.getName()+
                        " not null in "+mapper.method.toString());
            }
        }
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
    public void resultFilter(Object result) {
        Class<?> aClass = result.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);
            if(jsonIgnore != null) {
                try {
                    ObjectUtils.set(result, aClass, field, null);
                } catch (NoSuchMethodException |
                        InvocationTargetException |
                        IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
