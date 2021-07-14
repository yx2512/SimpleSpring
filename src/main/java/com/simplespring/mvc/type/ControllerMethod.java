package com.simplespring.mvc.type;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class ControllerMethod {
    private Class<?> controllerClass;
    private Method method;
    private Map<String, Parameter> methodParamMap;

    public ControllerMethod() {
    }

    public ControllerMethod(Class<?> controllerClass, Method method, Map<String, Parameter> methodParameters) {
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

    public Map<String, Parameter> getMethodParameters() {
        return methodParamMap;
    }

    public void setMethodParameters(Map<String, Parameter> methodParameters) {
        this.methodParamMap = methodParameters;
    }
}
