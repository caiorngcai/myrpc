package com.cai.rpc.server;

import com.cai.rpc.common.RpcRequest;
import com.cai.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 1.拿到request对象获取对象，反射调用方法
 * 2.拿到结果封装成response并写回到流水线
 * @author CRC
 * @create 2018-06-27 10:40
 **/
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Logger logger= LoggerFactory.getLogger(RpcHandler.class);
    private Map<String,Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap=handlerMap;
    }

    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        RpcResponse response=new RpcResponse();
        response.setRequestId(msg.getRequestId());
        try {
           Object result= handle(msg);
           response.setResult(result);
        }catch (Throwable throwable){
            response.setError(throwable);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);//写回到流水线

    }

    //获取request的属性，反射调用实现类的方法
    private Object handle(RpcRequest request) throws Throwable {
        String className=request.getClassName();
        Object serviceBean = handlerMap.get(className);//通过完整类名类实现的对象

        String methodName = request.getMethodName();//方法名
        Class<?>[] parameterTypes = request.getParameterTypes();//参数属性列表
        Object[] parameters = request.getParameters();//参数列表
        Class<?> aClass = Class.forName(className);//拿到接口类
        Method method = aClass.getMethod(methodName, parameterTypes);//达到接口的方法实例
        return method.invoke(serviceBean,parameters);//反射调用方法
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //捕获到异常
        logger.debug("服务器处理request消息时发生异常");
        ctx.close();
    }
}
