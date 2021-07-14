package com.simplespring.mvc.render.impl;

import com.google.gson.Gson;
import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.render.ResultRender;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class JSONResultRender implements ResultRender {
    private static final Gson gson = new Gson();
    private final String jsonStr;
    public JSONResultRender(Object result) {
        jsonStr = gson.toJson(result);
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        HttpServletResponse response = requestProcessorChain.getResponse();
        response.setContentType("application/json");
        try(PrintWriter writer = response.getWriter()) {
            writer.write(jsonStr);
            writer.flush();
        }
    }
}
