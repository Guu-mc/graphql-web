package com.github.mc.graphql.web.handle;

import com.github.mc.graphql.web.annotations.*;
import com.github.mc.graphql.web.core.MapperHandle;
import com.github.mc.graphql.web.utils.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DefaultMapperHandle implements MapperHandle, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public List<Mapper> getMappers() {
        ArrayList<Mapper> mappers = new ArrayList<>();
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Controller.class);
        for (Object value : beans.values()) {
            Class<?> aClass = value.getClass();
            Mapping mapping = aClass.getAnnotation(Mapping.class);
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                Query query = declaredMethod.getAnnotation(Query.class);
                Mutation mutation = declaredMethod.getAnnotation(Mutation.class);
                Subscription subscription = declaredMethod.getAnnotation(Subscription.class);
                Type type = declaredMethod.getAnnotation(Type.class);
                int count = ObjectUtils.notNullCount(query, mutation, subscription, type);
                if(count > 1) {
                    throw new RuntimeException("Query Mutation Subscription annotation cannot exist at the same time, "+aClass.toString()+" in "+declaredMethod.getName());
                }
                if(count == 1) {
                    Mapper mapper = new Mapper();
                    mapper.mapping = mapping;
                    mapper.aClass = aClass;
                    mapper.method = declaredMethod;
                    if(query != null) {
                        mapper.keyAnnotation = query;
                    }else if(mutation != null) {
                        mapper.keyAnnotation = mutation;
                    }else if(subscription != null){
                        mapper.keyAnnotation = subscription;
                    }else {
                        mapper.keyAnnotation = type;
                    }
                    mapper.parameters = declaredMethod.getParameters();
                    mapper.returnType = declaredMethod.getReturnType();
                    mappers.add(mapper);
                }
            }
        }
        return mappers;
    }
}
