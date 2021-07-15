package com.simplespring.mvc.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

public class ParameterWrapper {
    private Parameter parameter;
    private Class<? extends Annotation> annotationClass;
    private String name;
    private String defaultValue;
    private Boolean required;

    public ParameterWrapper(Parameter parameter) {
        this.parameter = parameter;
    }

    public ParameterWrapper(Parameter parameter, Class<? extends Annotation> annotationClass, String name, String defaultValue, Boolean required) {
        this.parameter = parameter;
        this.annotationClass = annotationClass;
        this.name = "".equals(name) ? parameter.getName() : name;
        this.defaultValue = "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n".equals(defaultValue) ? null : defaultValue;
        this.required = required;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
