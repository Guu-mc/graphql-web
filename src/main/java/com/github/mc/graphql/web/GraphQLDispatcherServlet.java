package com.github.mc.graphql.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.mc.graphql.web.core.ExecutionResultHandler;
import com.github.mc.graphql.web.core.GraphQLInvocation;
import com.github.mc.graphql.web.core.GraphQLInvocationData;
import com.github.mc.graphql.web.core.GraphQLRequestBody;
import com.github.mc.graphql.web.utils.IOUtils;
import graphql.ExecutionResult;
import graphql.Internal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Internal
@ComponentScan(basePackages = "com.github.mc.graphql.web")
@Component
public class GraphQLDispatcherServlet extends HttpServlet {

    @Autowired
    private GraphQLInvocation graphQLInvocation;

    @Autowired
    private ExecutionResultHandler executionResultHandler;

    @Autowired
    private ObjectMapper objectMapper;

    public GraphQLDispatcherServlet() {
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if (req instanceof HttpServletRequest && res instanceof HttpServletResponse) {
            HttpServletRequest request = (HttpServletRequest)req;
            HttpServletResponse response = (HttpServletResponse)res;
            cors(response);
            String method = request.getMethod();
            if("POST".equals(method)) {
                doPost(request, response);
            }
        } else {
            throw new ServletException("non-HTTP request or response");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GraphQLRequestBody body;
        String query = IOUtils.toString(req.getInputStream());
        try {
            body = objectMapper.readValue(query, GraphQLRequestBody.class);
        }catch (MismatchedInputException e) {
            return;
        }

        query = body.getQuery();
        if (query == null) query = "";
        CompletableFuture<ExecutionResult> executionResult = this.graphQLInvocation.invoke(new GraphQLInvocationData(query,
                body.getOperationName(), body.getVariables()), new HTTPRoot().setRequest(req).setResponse(resp));
        Object result = this.executionResultHandler.handleExecutionResult(executionResult);

        if(result instanceof CompletableFuture) {
            try {
                result = ((CompletableFuture) result).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("application/json;charset=UTF-8");
        IOUtils.write(objectMapper.
                        writeValueAsBytes(result),
                resp.getOutputStream()
        );
    }

    //    @PostMapping(GraphQLProperties.PREFIX)
//    public Object graphqlPOST(@RequestBody GraphQLRequestBody body, WebRequest webRequest) {
//        String query = body.getQuery();
//        if (query == null) query = "";
//        CompletableFuture<ExecutionResult> executionResult = this.graphQLInvocation.invoke(new GraphQLInvocationData(query, body.getOperationName(), body.getVariables()), webRequest);
//        return this.executionResultHandler.handleExecutionResult(executionResult);
//    }
//
//    //    @GetMapping("/")
//    public Object graphqlGET(@RequestParam("query") String query, @RequestParam(value = "operationName",required = false) String operationName, @RequestParam(value = "variables",required = false) String variablesJson, WebRequest webRequest) {
//        CompletableFuture<ExecutionResult> executionResult = this.graphQLInvocation.invoke(new GraphQLInvocationData(query, operationName, this.convertVariablesJson(variablesJson)), webRequest);
//        return this.executionResultHandler.handleExecutionResult(executionResult);
//    }

    private Map<String, Object> convertVariablesJson(String jsonMap) {
        if (jsonMap == null) return Collections.emptyMap();
        try {
            return objectMapper.readValue(jsonMap, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Could not convert variables GET parameter: expected a JSON map", e);
        }
    }

    public void cors(HttpServletResponse res) {
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "*");
        res.setHeader("Access-Control-Allow-Headers", "*");
        res.setHeader("Access-Control-Max-Age", "10000");
        res.setHeader("Access-Control-Allow-Credentials", "true");
    }
}
