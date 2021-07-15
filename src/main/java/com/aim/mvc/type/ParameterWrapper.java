package com.aim.mvc.type;

import java.lang.annotation.Annotation;

public class ParameterWrapper {
    private final Class<?> parameterClass;
    private Class<? extends Annotation> annotationClass;
    private String name;
    private String defaultValue;
    private Boolean required;

    public ParameterWrapper(Class<?> parameterClass) {
        this.parameterClass = parameterClass;
    }

    public ParameterWrapper(Class<?> parameterClass, Class<? extends Annotation> annotationClass, String name, String defaultValue, Boolean required) {
        this.parameterClass = parameterClass;
        this.annotationClass = annotationClass;
        this.name = name;
        this.defaultValue = "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n".equals(defaultValue) ? null : defaultValue;
        this.required = required;
    }

    public Class<?> getParameterClass() {
        return parameterClass;
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

}
