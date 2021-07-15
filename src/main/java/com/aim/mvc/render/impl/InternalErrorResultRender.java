package com.aim.mvc.render.impl;

import com.aim.mvc.RequestProcessorChain;
import com.aim.mvc.exception.BadRequestException;
import com.aim.mvc.exception.TypeConversionException;
import com.aim.mvc.render.ResultRender;

import javax.servlet.http.HttpServletResponse;

public class InternalErrorResultRender implements ResultRender {
    private final Exception e;

    public InternalErrorResultRender(Exception e) {
        this.e = e;
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        if(e instanceof BadRequestException) {
            requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, this.e.getMessage());
        } else if (e instanceof TypeConversionException) {
            requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, e.getMessage());
        } else {
            requestProcessorChain.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, this.e.getMessage());
        }
    }
}
