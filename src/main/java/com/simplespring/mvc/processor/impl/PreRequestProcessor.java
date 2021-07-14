package com.simplespring.mvc.processor.impl;

import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.processor.RequestProcessor;

public class PreRequestProcessor implements RequestProcessor {
    private static final String SUFFIX = "/";
    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getRequest().setCharacterEncoding("UTF-8");

        String requestPath = requestProcessorChain.getRequestPath();

        if(requestPath.length() > 1 && requestPath.endsWith(SUFFIX)) {
            requestProcessorChain.setRequestPath(requestPath.substring(0,requestPath.length()-1));
        }

        return true;
    }
}
