package com.simplespring.mvc.processor.impl;

import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

public class StaticResourceRequestProcessor implements RequestProcessor {
    private static final String DEFAULT_TOMCAT_SERVLET="default";
    private static final String STATIC_RESOURCE_PREFIX="/static/";

    RequestDispatcher defaultDispatcher;

    public StaticResourceRequestProcessor(ServletContext servletContext) {
        this.defaultDispatcher = servletContext.getNamedDispatcher(DEFAULT_TOMCAT_SERVLET);
        if(defaultDispatcher == null) {
            throw new RuntimeException("No default servlet found");
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        if(isStaticResource(requestProcessorChain.getRequestPath())) {
            defaultDispatcher.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }

    private boolean isStaticResource(String path) {
        return path.startsWith(STATIC_RESOURCE_PREFIX);
    }
}
