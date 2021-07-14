package com.simplespring.mvc.render.impl;

import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.render.ResultRender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InternalErrorResultRender implements ResultRender {
    private final String errMsg;

    public InternalErrorResultRender(String msg) {
        this.errMsg = msg;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, this.errMsg);
    }
}
