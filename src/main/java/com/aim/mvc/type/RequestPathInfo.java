package com.aim.mvc.type;

import java.util.Objects;

public class RequestPathInfo {
    private String httpMethod;
    private String httpPath;

    public RequestPathInfo() {
    }

    public RequestPathInfo(String httpMethod, String httpPath) {
        this.httpMethod = httpMethod;
        this.httpPath = httpPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public void setHttpPath(String httpPath) {
        this.httpPath = httpPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestPathInfo that = (RequestPathInfo) o;
        return httpMethod.equals(that.httpMethod) && httpPath.equals(that.httpPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, httpPath);
    }
}
