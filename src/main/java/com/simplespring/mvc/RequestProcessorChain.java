package com.simplespring.mvc;

import com.simplespring.mvc.exception.BadRequestException;
import com.simplespring.mvc.render.ResultRender;
import com.simplespring.mvc.processor.RequestProcessor;
import com.simplespring.mvc.render.impl.DefaultResultRender;
import com.simplespring.mvc.render.impl.InternalErrorResultRender;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

@Slf4j
public class RequestProcessorChain {
    private final Iterator<RequestProcessor> requestProcessorIterator;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final String requestMethod;
    private String requestPath;
    private ResultRender resultRender;

    public RequestProcessorChain(Iterator<RequestProcessor> requestProcessorIterator, HttpServletRequest request, HttpServletResponse response) {
        this.requestProcessorIterator = requestProcessorIterator;
        this.request = request;
        this.response = response;
        this.requestMethod = this.request.getMethod();
        this.requestPath = this.request.getPathInfo();
    }

    public void doRequestProcessorChain() {
        try{
            while(requestProcessorIterator.hasNext()) {
                if(!requestProcessorIterator.next().process(this)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurs when perform {} on {}", this.requestMethod, this.requestPath);
            this.resultRender = new InternalErrorResultRender(e);
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public void setResultRender(ResultRender resultRender) {
        this.resultRender = resultRender;
    }

    public void doRender() {
        if(this.resultRender == null) {
            this.resultRender = new DefaultResultRender();
        }

        try {
            this.resultRender.render(this);
        } catch (Exception e) {
            log.error("Rendering failed when perform {} with error message {}", this.requestMethod, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
