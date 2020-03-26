package com.github.mc.graphql.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({GraphQLProperties.class, GraphQLDispatcherServlet.class})
public class ServletConfig {
    @Autowired
    private GraphQLProperties graphQLProperties;
    @Autowired
    private GraphQLDispatcherServlet servlet;
    @Bean
    public ServletRegistrationBean<GraphQLDispatcherServlet> servletRegistrationBean() {
        return new ServletRegistrationBean(servlet, graphQLProperties.getMapping());
    }
}
