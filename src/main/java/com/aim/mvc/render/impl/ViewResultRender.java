package com.aim.mvc.render.impl;

import com.aim.mvc.RequestProcessorChain;
import com.aim.mvc.render.ResultRender;
import com.aim.mvc.type.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ViewResultRender implements ResultRender {
    private final ModelAndView modelAndView;

    public ViewResultRender(Object obj) {
        if(obj instanceof ModelAndView) {
            this.modelAndView = (ModelAndView) obj;
        } else if (obj instanceof String) {
            this.modelAndView = new ModelAndView().setView((String) obj);
        } else {
            throw new RuntimeException("Illegal type for view rendering");
        }
    }

    @Override
    public void render(RequestProcessorChain requestProcessorChain) throws Exception {
        HttpServletRequest request = requestProcessorChain.getRequest();
        HttpServletResponse response = requestProcessorChain.getResponse();

        String path = modelAndView.getView();
        Map<String, Object> model = modelAndView.getModel();

        for(Map.Entry<String, Object> entry: model.entrySet()) {
            request.setAttribute(entry.getKey(),entry.getValue());
        }

        request.getRequestDispatcher("/template/"+path).forward(request,response);
    }
}
