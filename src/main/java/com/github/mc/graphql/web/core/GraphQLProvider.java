package com.github.mc.graphql.web.core;

import com.github.mc.graphql.web.GraphQLProperties;
import com.github.mc.graphql.web.utils.IOUtils;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.*;
import java.util.List;

@Component
public class GraphQLProvider {
    private GraphQL graphQL;

    private Logger logger = LoggerFactory.getLogger(GraphQLProvider.class);

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @Autowired
    private GraphQLProperties graphQLProperties;
    @Autowired
    private MapperHandle mapperHandle;
    @Autowired
    private SDLHandle sdlHandle;
    @Autowired
    private RuntimeWiringHandle runtimeWiringHandle;

    @PostConstruct
    public void init() {
        List<MapperHandle.Mapper> mappers = mapperHandle.getMappers();

        if(graphQLProperties.getSdlPath() == null ||
                graphQLProperties.getSdlPath().trim().isEmpty()) {
            throw new RuntimeException("sdlPath or sdl-path not empty");
        }

        if(graphQLProperties.getSdlCreate()) {
            initSDL(mappers);
        }
        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(graphQLProperties.getSdlPath());
        String sdl;
        try {
            sdl = IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load file SDL: " + graphQLProperties.getSdlPath());
        }

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = runtimeWiringHandle.runtimeWiring(mappers);
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    public void initSDL(List<MapperHandle.Mapper> mappers) {
        String sdl = sdlHandle.sdl(mappers);
        String sdlPath = graphQLProperties.getSdlPath();
        //输出SDL文件
        outputSDL(sdl, sdlPath);
    }

    private void outputSDL(String sdl, String sdlPath) {
        try {
            String resource = getResource();
            if(resource == null) {
                logger.error("SDL file error: Unable to write file in jar package");
            }else {
                IOUtils.write(sdl.getBytes(), new FileOutputStream(resource + sdlPath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResource() {
        String resourcePath = getClass()
                .getClassLoader()
                .getResource("").getPath()
                .replace("/target/classes/", "/src/main/resources/");
        if(!new File(resourcePath).exists()) {
            resourcePath = null;
        }
        return resourcePath;
    }
}