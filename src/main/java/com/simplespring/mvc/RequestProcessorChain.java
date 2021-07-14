package com.simplespring.mvc;

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
    private Iterator<RequestProcessor> requestProcessorIterator;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private String requestMethod;
    private String requestPath;
    private int responseCode;
    private ResultRender resultRender;

    public RequestProcessorChain(Iterator<RequestProcessor> requestProcessorIterator, HttpServletRequest request, HttpServletResponse response) {
        this.requestProcessorIterator = requestProcessorIterator;
        this.request = request;
        this.response = response;
        this.requestMethod = this.request.getMethod();
        this.requestPath = this.request.getPathInfo();
        this.responseCode = HttpServletResponse.SC_OK;
    }

    public void doRequestProcessorChain() {
        try{
            while(requestProcessorIterator.hasNext()) {
                if(!requestProcessorIterator.next().process(this)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error occurs when perform " + this.requestMethod);
            this.resultRender = new InternalErrorResultRender(e.getMessage());
        }
    }

    public Iterator<RequestProcessor> getRequestProcessorIterator() {
        return requestProcessorIterator;
    }

    public void setRequestProcessorIterator(Iterator<RequestProcessor> requestProcessorIterator) {
        this.requestProcessorIterator = requestProcessorIterator;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public ResultRender getResultRender() {
        return resultRender;
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
            log.error("Rendering failed when perform " + this.requestMethod);
            throw new RuntimeException(e);
        }
    }
}
