package com.github.mc.graphql.web;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class HTTPRoot {
    private ServletRequest request;
    private ServletResponse response;

    public ServletRequest getRequest() {
        return request;
    }

    public HTTPRoot setRequest(ServletRequest request) {
        this.request = request;
        return this;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public HTTPRoot setResponse(ServletResponse response) {
        this.response = response;
        return this;
    }
}
