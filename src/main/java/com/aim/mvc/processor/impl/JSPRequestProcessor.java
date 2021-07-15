package com.aim.mvc.processor.impl;

import com.aim.mvc.RequestProcessorChain;
import com.aim.mvc.processor.RequestProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

public class JSPRequestProcessor implements RequestProcessor {
    private static final String JSP_SERVLET = "jsp";
    private static final String JSP_RESOURCE_PREFIX = "/template/";

    private final RequestDispatcher requestDispatcher;

    public JSPRequestProcessor(ServletContext servletContext) {
        requestDispatcher = servletContext.getNamedDispatcher(JSP_SERVLET);
        if(requestDispatcher == null) {
            throw new RuntimeException("No JSP servlet found");
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        if(isJSPResource(requestProcessorChain.getRequestPath())) {
            requestDispatcher.forward(requestProcessorChain.getRequest(), requestProcessorChain.getResponse());
            return false;
        }
        return true;
    }

    private boolean isJSPResource(String path) {
        return path.startsWith(JSP_RESOURCE_PREFIX);
    }
}
