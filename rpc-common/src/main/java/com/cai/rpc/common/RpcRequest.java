package com.cai.rpc.common;

/**
 * 封装RPC客户端向服务器发送的反射信息
 *
 * @author CRC
 * @create 2018-06-26 18:57
 **/
public class RpcRequest {
    private String requestId;//请求封装信息的id
    private String className;//类名
    private String methodName;//方法名
    private Class<?>[] parameterTypes;//参数类型
    private Object[] parameters;//参数列表

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
}
