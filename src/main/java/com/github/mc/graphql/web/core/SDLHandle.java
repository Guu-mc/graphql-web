package com.github.mc.graphql.web.core;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public interface SDLHandle {

    String sdl(List<MapperHandle.Mapper> mappers);

    String sdlMethod(MapperHandle.Mapper mapper, boolean returnNotNull, Set<Class<?>> typeSet);

    List<Parameter> filterParameter(Parameter[] parameters);

    String sdlParameter(List<Parameter> parameters, Set<Class<?>> typeSet);

    boolean parameterNotNull(Parameter parameter);

    boolean fieldNotNull(Field field);

    String sdlType(LinkedHashSet<Class<?>> typeSet);

    List<Field> filterFields(Field[] fields);

    boolean arrayNotNull(AnnotatedElement element);
}
