package com.simplespring.mvc.processor.impl;

import com.simplespring.core.context.BeanContainer;
import com.simplespring.core.utils.ConversionUtil;
import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.annotation.RequestMapping;
import com.simplespring.mvc.annotation.RequestParam;
import com.simplespring.mvc.annotation.ResponseBody;
import com.simplespring.mvc.processor.RequestProcessor;
import com.simplespring.mvc.render.ResultRender;
import com.simplespring.mvc.render.impl.JSONResultRender;
import com.simplespring.mvc.render.impl.ResourceNotFoundResultRender;
import com.simplespring.mvc.render.impl.ViewResultRender;
import com.simplespring.mvc.type.ControllerMethod;
import com.simplespring.mvc.type.RequestPathInfo;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {
    private final BeanContainer beanContainer;
    private final Map<RequestPathInfo, ControllerMethod> pathInfoControllerMethodMap = new ConcurrentHashMap<>();

    public ControllerRequestProcessor() {
        this.beanContainer = BeanContainer.getInstance();
        Set<Map.Entry<String, Object>> beansWithRequestMapping =
                beanContainer.getBeansByAnnotation(RequestMapping.class);
        initPathInfoControllerMethodMap(beansWithRequestMapping);
    }

    private void initPathInfoControllerMethodMap(Set<Map.Entry<String, Object>> beansWithRequestMapping) {
        if(beansWithRequestMapping == null || beansWithRequestMapping.size() == 0) {
            return;
        }
        for (Map.Entry<String, Object> entry : beansWithRequestMapping) {
            Class<?> clazz = entry.getValue().getClass();
            RequestMapping classRequest = clazz.getDeclaredAnnotation(RequestMapping.class);
            String basePath = classRequest.value();
            if(!basePath.startsWith("/")) {
                basePath += "/";
            }

            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if(method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodRequest = method.getDeclaredAnnotation(RequestMapping.class);
                    String secondaryPath = methodRequest.value();
                    if(!secondaryPath.startsWith("/")) {
                        secondaryPath += "/";
                    }

                    String finalPath = basePath + secondaryPath;

                    Map<String, Parameter> methodParam = new HashMap<>();

                    Parameter[] parameters = method.getParameters();

                    for (Parameter parameter : parameters) {
                        RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
                        if(paramAnnotation == null) {
                            if(parameter.getType().equals(HttpServletRequest.class)) {
                                methodParam.put("HttpServletRequest", parameter);
                            } else if (parameter.getType().equals(HttpServletResponse.class)) {
                                methodParam.put("HttpServletResponse", parameter);
                            } else {
                                throw new RuntimeException("Floating parameter: " + parameter.getName());
                            }
                        } else {
                            String paramName = paramAnnotation.value();
                            if("".equals(paramName)) {
                                paramName = parameter.getName();
                            }
                            methodParam.put(paramName,parameter);
                        }
                    }
                    String httpMethod = String.valueOf(methodRequest.method());

                    RequestPathInfo requestPathInfo = new RequestPathInfo(httpMethod, finalPath);
                    if(this.pathInfoControllerMethodMap.containsKey(requestPathInfo)) {
                        log.warn("duplicate url: {} registration, current class {} method {} will override the former one",
                                requestPathInfo.getHttpPath(), clazz.getName(), method.getName());
                    }

                    ControllerMethod controllerMethod = new ControllerMethod(clazz, method, methodParam);
                    this.pathInfoControllerMethodMap.put(requestPathInfo, controllerMethod);
                }
            }
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        String requestPath = requestProcessorChain.getRequestPath();
        String requestMethod = requestProcessorChain.getRequestMethod();

        ControllerMethod controllerMethod = this.pathInfoControllerMethodMap.get(new RequestPathInfo(requestMethod, requestPath));
        if(controllerMethod == null) {
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(requestMethod, requestPath));
            return false;
        }

        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest());

        setResultRender(result, controllerMethod, requestProcessorChain);
        return true;
    }

    private void setResultRender(Object result, ControllerMethod controllerMethod, RequestProcessorChain requestProcessorChain) {
        if(result == null) {
            return;
        }
        ResultRender resultRender;
        boolean isJSON = controllerMethod.getMethod().isAnnotationPresent(ResponseBody.class);
        if(isJSON) {
            resultRender = new JSONResultRender(result);
        } else {
            resultRender = new ViewResultRender(result);
        }
        requestProcessorChain.setResultRender(resultRender);
    }

    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request) {
        Map<String, String> requestParamMap = new HashMap<>();
        Map parameterMap = request.getParameterMap();

        for(Map.Entry<String, String []> entry : (Set<Map.Entry<String, String []>>) parameterMap.entrySet()) {
            if(entry.getValue() != null) {
                requestParamMap.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        List<Object> methodParams = new ArrayList<>();
        Map<String, Parameter> methodParamMap = controllerMethod.getMethodParameters();
        for(String name : methodParamMap.keySet()) {
            Parameter parameter = methodParamMap.get(name);
            RequestParam argAnnotation = parameter.getAnnotation(RequestParam.class);

            if(argAnnotation == null) {
                continue;
            }

            Object afterConversion;
            String value = requestParamMap.get(name);
            if(value == null && argAnnotation.required()) {
                throw new RuntimeException("Missing request argument: " + name);
            } else if (value == null) {
                if(argAnnotation.defaultValue().equals("\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n")) {
                    afterConversion = ConversionUtil.primitiveNull(parameter.getType());
                } else {
                    afterConversion = ConversionUtil.convert(parameter.getType(),argAnnotation.defaultValue());
                }
            } else {
                afterConversion = ConversionUtil.convert(parameter.getType(), value);
            }

            methodParams.add(afterConversion);
        }
        Object targetController = beanContainer.getBean(controllerMethod.getControllerClass().getSimpleName());
        Method targetMethod = controllerMethod.getMethod();

        targetMethod.setAccessible(true);
        Object result;

        try{
            if(methodParams.size() == 0) {
                result = targetMethod.invoke(targetController);
            } else {
                result = targetMethod.invoke(targetController, methodParams.toArray());
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
