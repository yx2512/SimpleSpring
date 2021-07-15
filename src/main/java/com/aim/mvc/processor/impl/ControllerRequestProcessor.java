package com.aim.mvc.processor.impl;

import com.google.gson.Gson;
import com.aim.core.annotation.Controller;
import com.aim.core.context.BeanContainer;
import com.aim.core.utils.ConversionUtil;
import com.aim.mvc.RequestProcessorChain;
import com.aim.mvc.annotation.*;
import com.aim.mvc.exception.BadRequestException;
import com.aim.mvc.exception.ParameterBindingException;
import com.aim.mvc.processor.RequestProcessor;
import com.aim.mvc.render.ResultRender;
import com.aim.mvc.render.impl.JSONResultRender;
import com.aim.mvc.render.impl.ResourceNotFoundResultRender;
import com.aim.mvc.render.impl.ViewResultRender;
import com.aim.mvc.type.ControllerMethod;
import com.aim.mvc.type.FuzzyRequestPathInfo;
import com.aim.mvc.type.ParameterWrapper;
import com.aim.mvc.type.RequestPathInfo;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ControllerRequestProcessor implements RequestProcessor {
    private final BeanContainer beanContainer;
    private final Map<RequestPathInfo, ControllerMethod> pathToMethodInControllerMapping = new ConcurrentHashMap<>();
    private final Map<FuzzyRequestPathInfo, ControllerMethod> fuzzyPathToMethodInControllerMapping = new ConcurrentHashMap<>();

    public ControllerRequestProcessor() {
        this.beanContainer = BeanContainer.getInstance();
        Set<Map.Entry<String, Object>> controllerBeans =
                beanContainer.getBeansByAnnotation(Controller.class);
        initPathInfoControllerMethodMap(controllerBeans);
    }

    private void initPathInfoControllerMethodMap(Set<Map.Entry<String, Object>> controllerBeans) {
        if(controllerBeans == null || controllerBeans.size() == 0) {
            return;
        }
        for (Map.Entry<String, Object> entry : controllerBeans) {
            Class<?> clazz = entry.getValue().getClass();

            StringBuilder pathBuilder = new StringBuilder();
            if(clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping classRequest = clazz.getDeclaredAnnotation(RequestMapping.class);
                String basePath = classRequest.value();
                if(!basePath.startsWith("/")) {
                    pathBuilder.append("/");
                }
                pathBuilder.append(basePath);
            }
            int basePathLen = pathBuilder.length();
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                pathBuilder.setLength(basePathLen);
                if(method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodRequest = method.getDeclaredAnnotation(RequestMapping.class);
                    String secondaryPath = methodRequest.value();
                    if(!secondaryPath.startsWith("/")) {
                        pathBuilder.append("/");
                    }

                    String finalPath = pathBuilder.append(secondaryPath).toString();

                    Map<String, ParameterWrapper> methodParamMap = new HashMap<>();
                    List<String> methodParams = new ArrayList<>();

                    buildMethodParamMap(methodParams, methodParamMap, method);
                    ControllerMethod controllerMethod = new ControllerMethod(finalPath, clazz, method, methodParams, methodParamMap);

                    if(finalPath.contains("{")) {
                        FuzzyRequestPathInfo fuzzyRequestPathInfo = new FuzzyRequestPathInfo(String.valueOf(methodRequest.method()), finalPath);
                        if(this.fuzzyPathToMethodInControllerMapping.containsKey(fuzzyRequestPathInfo)) {
                            log.warn("duplicate url: {} registration, current class {} method {} will override the former one",
                                    fuzzyRequestPathInfo.getHttpPath(), clazz.getName(), method.getName());
                        }
                        this.fuzzyPathToMethodInControllerMapping.put(fuzzyRequestPathInfo, controllerMethod);
                    } else {
                        RequestPathInfo requestPathInfo = new RequestPathInfo(String.valueOf(methodRequest.method()), finalPath);
                        if(this.pathToMethodInControllerMapping.containsKey(requestPathInfo)) {
                            log.warn("duplicate url: {} registration, current class {} method {} will override the former one",
                                    requestPathInfo.getHttpPath(), clazz.getName(), method.getName());
                        }
                        this.pathToMethodInControllerMapping.put(requestPathInfo, controllerMethod);
                    }
                }
            }
        }
    }

    @Override
    public boolean process(RequestProcessorChain requestProcessorChain) throws Exception {
        String requestPath = requestProcessorChain.getRequestPath();
        String requestMethod = requestProcessorChain.getRequestMethod();

        RequestPathInfo requestPathInfo = new RequestPathInfo(requestMethod, requestPath);
        ControllerMethod methodInController = this.pathToMethodInControllerMapping.get(requestPathInfo);

        Map<String, String> pathVariableMap = new HashMap<>();

        if(methodInController == null) {
            methodInController = matchFuzzyPathInfo(requestPath,pathVariableMap);
        }

        if(methodInController == null) {
            requestProcessorChain.setResultRender(new ResourceNotFoundResultRender(requestMethod, requestPath));
            return false;
        }

        Object result = invokeMethodInController(methodInController, pathVariableMap, requestProcessorChain.getRequest(), requestProcessorChain.getResponse());

        setResultRender(result, methodInController, requestProcessorChain);
        return true;
    }

    private void buildMethodParamMap(List<String> methodParams, Map<String, ParameterWrapper> methodParamMap, Method method) {
        Parameter[] parameters = method.getParameters();

        for (Parameter parameter : parameters) {
            ParameterWrapper parameterWrapper;
            if(parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                parameterWrapper = new ParameterWrapper(parameter.getType(),RequestParam.class,requestParam.value(),requestParam.defaultValue(),requestParam.required());
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
                parameterWrapper = new ParameterWrapper(parameter.getType(), RequestBody.class, null, null, requestBody.required());
            } else if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                parameterWrapper = new ParameterWrapper(parameter.getType(), PathVariable.class, pathVariable.value(), pathVariable.defaultValue(), pathVariable.required());
            } else {
                if(parameter.getType().equals(HttpServletRequest.class)) {
                    parameterWrapper = new ParameterWrapper(parameter.getType());
                } else if (parameter.getType().equals(HttpServletResponse.class)) {
                    parameterWrapper = new ParameterWrapper(parameter.getType());
                } else {
                    throw new RuntimeException(String.format("Unbounded method argument %s in method %s.",parameter.getName(),method.getName()));
                }
            }
            methodParams.add(parameterWrapper.getName());
            methodParamMap.put(parameterWrapper.getName(),parameterWrapper);
        }
    }

    private ControllerMethod matchFuzzyPathInfo(String requestPath, Map<String, String> pathVariableMap) {
        Set<FuzzyRequestPathInfo> fuzzyRequestPathInfos = fuzzyPathToMethodInControllerMapping.keySet();
        for(FuzzyRequestPathInfo pathInfo : fuzzyRequestPathInfos) {
            Pattern pattern = Pattern.compile(pathInfo.getHttpPath());
            Matcher matcher = pattern.matcher(requestPath);

            if(matcher.matches()) {
                for(int i=0; i<matcher.groupCount(); i++) {
                    pathVariableMap.put(pathInfo.getPathVariables().get(i), matcher.group(i+1));
                }

                ControllerMethod controllerMethod = fuzzyPathToMethodInControllerMapping.get(pathInfo);
                controllerMethod.setPath(pathInfo.getHttpPath());
                return controllerMethod;
            }
        }
        return null;
    }

    private Object invokeMethodInController(ControllerMethod methodInController, Map<String, String> pathVariableMap, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> requestParamMap = getRequestParamMap(request);
        List<Object> invocationArgs = new ArrayList<>();
        List<String> paramsInMethod = methodInController.getMethodParams();

        Map<String, ParameterWrapper> paramsInMethodMap = methodInController.getMethodParamMap();

        for(String paramName : paramsInMethod) {
            ParameterWrapper parameterWrapper = paramsInMethodMap.get(paramName);
            Class<? extends Annotation> argAnnotationClass = parameterWrapper.getAnnotationClass();

            Object afterConversion;
            if(argAnnotationClass == null) {
                if(parameterWrapper.getParameterClass().equals(HttpServletRequest.class)) {
                    invocationArgs.add(request);
                } else if (parameterWrapper.getParameterClass().equals(HttpServletResponse.class)) {
                    invocationArgs.add(response);
                }
                continue;
            } else if(argAnnotationClass.equals(RequestParam.class)){
                String valueFromRequest = requestParamMap.get(paramName);
                afterConversion = convertPrimitiveRequestParameter(valueFromRequest, parameterWrapper);
            } else if(argAnnotationClass.equals(PathVariable.class)) {
                afterConversion = convertPrimitiveRequestParameter(paramName, pathVariableMap, parameterWrapper);
            } else if(argAnnotationClass.equals(RequestBody.class)) {
                try {
                    String jsonStr = request.getReader().lines().collect(Collectors.joining());
                    afterConversion = convertJSONRequestParameter(jsonStr,parameterWrapper);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot read from request");
                }
            } else {
                log.error("Unrecognized annotation {} on parameter {} in method {}", argAnnotationClass, parameterWrapper.getParameterClass(), methodInController.getMethod());
                throw new ParameterBindingException("Request parameter binding failed on " + paramName);
            }

            invocationArgs.add(afterConversion);
        }
        Object targetController = beanContainer.getBean(methodInController.getControllerClass().getSimpleName());
        Method targetMethod = methodInController.getMethod();

        targetMethod.setAccessible(true);
        Object result;

        try{
            if(invocationArgs.size() == 0) {
                result = targetMethod.invoke(targetController);
            } else {
                result = targetMethod.invoke(targetController, invocationArgs.toArray());
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
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

    private Map<String, String> getRequestParamMap(HttpServletRequest request) {
        Map<String, String> requestParamMap = new HashMap<>();
        Map parameterMap = request.getParameterMap();

        for(Map.Entry<String, String []> entry : (Set<Map.Entry<String, String []>>) parameterMap.entrySet()) {
            if(entry.getValue() != null) {
                requestParamMap.put(entry.getKey(), entry.getValue()[0]);
            }
        }
        return requestParamMap;
    }

    private Object convertPrimitiveRequestParameter(String value, ParameterWrapper parameterWrapper) {
        Object afterConversion;
        if(value == null && parameterWrapper.getRequired()) {
            throw new BadRequestException("Missing request argument: " + parameterWrapper.getName());
        } else if (value == null) {
            if(parameterWrapper.getDefaultValue() == null) {
                afterConversion = ConversionUtil.primitiveNull(parameterWrapper.getParameterClass());
            } else {
                afterConversion = ConversionUtil.convert(parameterWrapper.getParameterClass(), parameterWrapper.getDefaultValue());
            }
        } else {
            afterConversion = ConversionUtil.convert(parameterWrapper.getParameterClass(), value);
        }

        return afterConversion;
    }

    private Object convertPrimitiveRequestParameter(String name, Map<String, String> segmentMap, ParameterWrapper parameterWrapper) {
        return convertPrimitiveRequestParameter(segmentMap.getOrDefault(name, null), parameterWrapper);
    }

    private Object convertJSONRequestParameter(String value, ParameterWrapper parameterWrapper) {
        if(value == null && parameterWrapper.getRequired() || "{}".equals(value) && parameterWrapper.getRequired()) {
            throw new BadRequestException("Missing request body: " + parameterWrapper.getParameterClass());
        } else if (value == null || "{}".equals(value)) {
            return null;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(value, parameterWrapper.getParameterClass());
        }
    }
}
