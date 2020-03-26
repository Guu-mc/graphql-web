package com.github.mc.graphql.web.core;

import graphql.schema.idl.RuntimeWiring;

import java.util.List;

public interface RuntimeWiringHandle {

    RuntimeWiring runtimeWiring(List<MapperHandle.Mapper> mappers);

    void resultFilter(Object result);
}
