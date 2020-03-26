package com.github.mc.graphql.web.core;

import com.github.mc.graphql.web.annotations.Mapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public interface MapperHandle {

    List<Mapper> getMappers();

    class Mapper {
        public Mapping mapping;
        public Class<?> aClass;
        public Object keyAnnotation;
        public Method method;
        public Parameter[] parameters;
        public Class<?> returnType;
    }
}
