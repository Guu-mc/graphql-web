package com.github.mc.graphql.web.core;

import com.github.mc.graphql.web.HTTPRoot;
import graphql.ExecutionResult;
import graphql.PublicApi;

import java.util.concurrent.CompletableFuture;

@PublicApi
public interface GraphQLInvocation {
    CompletableFuture<ExecutionResult> invoke(GraphQLInvocationData invocationData,
                                              HTTPRoot root);
}