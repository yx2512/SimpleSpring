package com.simplespring.mvc.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FuzzyRequestPathInfo {
    private final String httpMethod;
    private final String httpPath;
    private final List<String> pathVariables;

    public FuzzyRequestPathInfo(String method, String path) {
        this.httpMethod = method;
        this.pathVariables = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(path);
        while(matcher.find()) {
            pathVariables.add(matcher.group(1));
        }
        path = path.replaceAll("/", "\\\\/");
        this.httpPath = path.replaceAll("\\{.+?\\}","(.+?)");
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public List<String> getPathVariables() {
        return pathVariables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuzzyRequestPathInfo that = (FuzzyRequestPathInfo) o;
        return httpMethod.equals(that.httpMethod) && httpPath.equals(that.httpPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, httpPath);
    }
}
