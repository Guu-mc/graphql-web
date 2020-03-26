package com.github.mc.graphql.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(
        prefix = GraphQLProperties.PREFIX
)
public class GraphQLProperties {
    public static final String PREFIX = "graphql-web";

    /**
     * GraphQLServlet 访问地址, 值不能为 "/", 请求地址 "/" 被 DispatcherServlet 覆盖(graphql-web 设计是和 Spring mvc 共存方式)
     */
    private String mapping = "/graphql";
    /**
     * 是否自动解析并创建 sdl 文件
     */
    private boolean sdlCreate = false;
    /**
     * sdl 文件名
     */
    private String sdlPath = "schema.graphqls";
    /**
     * 默认主键名称
     */
    private String idField = "id";

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public boolean getSdlCreate() {
        return sdlCreate;
    }

    public void setSdlCreate(boolean sdlCreate) {
        this.sdlCreate = sdlCreate;
    }

    public String getSdlPath() {
        return sdlPath;
    }

    public void setSdlPath(String sdlPath) {
        this.sdlPath = sdlPath;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }
}
