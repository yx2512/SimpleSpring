package com.aim.mvc.type;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ControllerMethod {
    private String path;
    private Class<?> controllerClass;
    private Method method;
    private List<String> methodParams;
    private Map<String, ParameterWrapper> methodParamMap;

    public ControllerMethod() {
    }

    public ControllerMethod(String path, Class<?> controllerClass, Method method, List<String> methodParams ,Map<String, ParameterWrapper> methodParameters) {
        this.path = path;
        this.controllerClass = controllerClass;
        this.method = method;
        this.methodParamMap = methodParameters;
        this.methodParams = methodParams;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Map<String, ParameterWrapper> getMethodParamMap() {
        return methodParamMap;
    }

    public String getPath() {
        return path;
    }

    public List<String> getMethodParams() {
        return methodParams;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
