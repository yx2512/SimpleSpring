package com.simplespring.mvc.processor.impl;

import com.google.gson.Gson;
import com.simplespring.core.context.BeanContainer;
import com.simplespring.core.utils.ConversionUtil;
import com.simplespring.mvc.RequestProcessorChain;
import com.simplespring.mvc.annotation.*;
import com.simplespring.mvc.processor.RequestProcessor;
import com.simplespring.mvc.render.ResultRender;
import com.simplespring.mvc.render.impl.JSONResultRender;
import com.simplespring.mvc.render.impl.ResourceNotFoundResultRender;
import com.simplespring.mvc.render.impl.ViewResultRender;
import com.simplespring.mvc.type.ControllerMethod;
import com.simplespring.mvc.type.ParameterWrapper;
import com.simplespring.mvc.type.RequestPathInfo;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

                    Map<String, ParameterWrapper> methodParam = new HashMap<>();

                    Parameter[] parameters = method.getParameters();

                    for (Parameter parameter : parameters) {
                        ParameterWrapper parameterWrapper;
                        if(parameter.isAnnotationPresent(RequestParam.class)) {
                            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                            parameterWrapper = new ParameterWrapper(parameter,RequestParam.class,requestParam.value(),requestParam.defaultValue(),requestParam.required());
                        } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                            parameterWrapper = new ParameterWrapper(parameter, RequestBody.class, null, null, requestBody.required());
                        } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                            parameterWrapper = new ParameterWrapper(parameter, PathVariable.class, pathVariable.value(), pathVariable.defaultValue(), pathVariable.required());
                        } else {
                            if(parameter.getType().equals(HttpServletRequest.class)) {
                                parameterWrapper = new ParameterWrapper(parameter);
                            } else if (parameter.getType().equals(HttpServletResponse.class)) {
                                parameterWrapper = new ParameterWrapper(parameter);
                            } else {
                                throw new RuntimeException("Floating parameter: " + parameter.getName());
                            }
                        }
                        methodParam.put(parameter.getName(),parameterWrapper);
                    }
                    String httpMethod = String.valueOf(methodRequest.method());

                    RequestPathInfo requestPathInfo = new RequestPathInfo(httpMethod, finalPath);
                    if(this.pathInfoControllerMethodMap.containsKey(requestPathInfo)) {
                        log.warn("duplicate url: {} registration, current class {} method {} will override the former one",
                                requestPathInfo.getHttpPath(), clazz.getName(), method.getName());
                    }

                    ControllerMethod controllerMethod = new ControllerMethod(finalPath, clazz, method, methodParam);
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

        Object result = invokeControllerMethod(controllerMethod, requestProcessorChain.getRequest(), requestProcessorChain.getResponse());

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

    private Map<String, String> buildUrlSegmentMap(String urlAboveMethod,String requestPath) {
        if(!urlAboveMethod.contains("{")) {
            return null;
        }
        Map<String, String> segmentMap = new HashMap<>();
        String[] aboveMethodUrlSegments = urlAboveMethod.split("/");
        String[] requestUrlSegments = requestPath.split("/");

        for(int i=0; i<Math.min(aboveMethodUrlSegments.length, requestUrlSegments.length); i++) {
            if(aboveMethodUrlSegments[i].startsWith("{") && aboveMethodUrlSegments[i].endsWith("}")) {
                segmentMap.put(aboveMethodUrlSegments[i].substring(1,aboveMethodUrlSegments[i].length()-1), requestUrlSegments[i]);
            }
        }

        return segmentMap;
    }

    private Object invokeControllerMethod(ControllerMethod controllerMethod, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> requestParamMap = new HashMap<>();
        Map parameterMap = request.getParameterMap();

        Map<String, String> urlSegmentMap = buildUrlSegmentMap(controllerMethod.getPath(), request.getPathInfo());

        for(Map.Entry<String, String []> entry : (Set<Map.Entry<String, String []>>) parameterMap.entrySet()) {
            if(entry.getValue() != null) {
                requestParamMap.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        List<Object> methodParams = new ArrayList<>();
        Map<String, ParameterWrapper> methodParamMap = controllerMethod.getMethodParameters();
        for(String name : methodParamMap.keySet()) {
            ParameterWrapper parameterWrapper = methodParamMap.get(name);
            Class<? extends Annotation> argAnnotationClass = parameterWrapper.getAnnotationClass();
            Object afterConversion;
            if(argAnnotationClass == null) {
                if(parameterWrapper.getParameter().getType().equals(HttpServletRequest.class)) {
                    methodParams.add(request);
                } else if (parameterWrapper.getParameter().getType().equals(HttpServletResponse.class)) {
                    methodParams.add(response);
                }
                continue;
            } else if(argAnnotationClass.equals(RequestParam.class)){
                String value = requestParamMap.get(name);
                afterConversion = convertPrimitiveRequestParameter(value, parameterWrapper);
            } else if(argAnnotationClass.equals(PathVariable.class)) {
                if(urlSegmentMap == null || urlSegmentMap.size() == 0) {
                    throw new RuntimeException("Did not find corresponding url pattern in method: " + controllerMethod.getMethodParameters());
                }
                afterConversion = convertPrimitiveRequestParameter(name, urlSegmentMap, parameterWrapper);
            } else if(argAnnotationClass.equals(RequestBody.class)) {
                try {
                    String jsonStr = request.getReader().lines().collect(Collectors.joining());
                    afterConversion = convertJSONRequestParameter(jsonStr,parameterWrapper);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read from request");
                }
            } else {
                log.error("Unrecognized annotation {} on parameter {} in method {}", argAnnotationClass, parameterWrapper.getParameter().getName(), controllerMethod.getMethod());
                throw new RuntimeException("Request parameter binding failed");
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

    private Object convertPrimitiveRequestParameter(String value, ParameterWrapper parameterWrapper) {
        Object afterConversion;
        if(value == null && parameterWrapper.getRequired()) {
            throw new RuntimeException("Missing request argument: " + parameterWrapper.getName());
        } else if (value == null) {
            if(parameterWrapper.getDefaultValue() == null) {
                afterConversion = ConversionUtil.primitiveNull(parameterWrapper.getParameter().getType());
            } else {
                afterConversion = ConversionUtil.convert(parameterWrapper.getParameter().getType(), parameterWrapper.getDefaultValue());
            }
        } else {
            afterConversion = ConversionUtil.convert(parameterWrapper.getParameter().getType(), value);
        }

        return afterConversion;
    }

    private Object convertPrimitiveRequestParameter(String name, Map<String, String> segmentMap, ParameterWrapper parameterWrapper) {
        return convertPrimitiveRequestParameter(segmentMap.getOrDefault(name, null), parameterWrapper);
    }

    private Object convertJSONRequestParameter(String value, ParameterWrapper parameterWrapper) {
        Gson gson = new Gson();
        return gson.fromJson(value, parameterWrapper.getParameter().getType());
    }
}
