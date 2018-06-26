package com.cai.rpc.common;

/**
 * 服务器向客户端发送消息的封装
 *
 * @author CRC
 * @create 2018-06-26 20:13
 **/
public class RpcResponse {
    private String requestId;//对应的requestId
    private Throwable error;//调用过程中抛出的异常信息
    private Object result;//返回的结果封装

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
