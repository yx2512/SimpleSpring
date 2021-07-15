package com.aim.mvc.render.impl;

import com.aim.mvc.RequestProcessorChain;
import com.aim.mvc.render.ResultRender;

import javax.servlet.http.HttpServletResponse;

public class DefaultResultRender implements ResultRender {
    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        HttpServletResponse response = requestProcessorChain.getResponse();
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
