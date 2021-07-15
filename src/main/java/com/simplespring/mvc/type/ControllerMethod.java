package com.simplespring.mvc.type;

import java.lang.reflect.Method;
import java.util.Map;

public class ControllerMethod {
    private String path;
    private Class<?> controllerClass;
    private Method method;
    private Map<String, ParameterWrapper> methodParamMap;

    public ControllerMethod() {
    }

    public ControllerMethod(String path, Class<?> controllerClass, Method method, Map<String, ParameterWrapper> methodParameters) {
        this.path = path;
        this.controllerClass = controllerClass;
        this.method = method;
        this.methodParamMap = methodParameters;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Map<String, ParameterWrapper> getMethodParameters() {
        return methodParamMap;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
