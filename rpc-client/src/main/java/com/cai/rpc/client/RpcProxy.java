package com.cai.rpc.client;

import com.cai.rpc.common.RpcRequest;
import com.cai.rpc.common.RpcResponse;
import com.cai.rpc.registry.ServiceDiscovey;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * 创建代理对象的方法
 *
 * @author CRC
 * @create 2018-06-27 14:24
 **/
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovey serviceDiscovey;

    public RpcProxy(String serverAddress){
        this.serverAddress=serverAddress;
    }
    public RpcProxy(ServiceDiscovey serviceDiscovey){
        this.serviceDiscovey=serviceDiscovey;
    }

    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //封装request对象
                RpcRequest rpcRequest=new RpcRequest();
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setClassName(method.getDeclaringClass().getName());
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setParameters(args);
                rpcRequest.setParameterTypes(method.getParameterTypes());
                //查找服务
                if(serviceDiscovey!=null){
                    String serverAddress = serviceDiscovey.discover();
                }
                String[] array = serverAddress.split(":");
                String host=array[0];
                int port=Integer.parseInt(array[1]);
                RpcClient client=new RpcClient(host,port);
                RpcResponse response = client.send(rpcRequest);
                if(response.getError()!=null){
                    throw response.getError();
                }else {
                    return response.getResult();
                }
            }
        });
    }
}
