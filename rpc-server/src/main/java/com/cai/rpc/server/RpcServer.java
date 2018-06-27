package com.cai.rpc.server;

import com.cai.rpc.common.RpcDecoder;
import com.cai.rpc.common.RpcEncoder;
import com.cai.rpc.common.RpcRequest;
import com.cai.rpc.common.RpcResponse;
import com.cai.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.xml.bind.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 框架的RPC 服务器（用于将用户系统的业务类发布为 RPC 服务）
 * 使用时可由用户通过spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了ApplicationContextAware InitializingBean
 * spring构造本对象时会调用setApplicationContext()方法，从而可以在方法中通过自定义注解获得用户的业务接口和实现
 * 还会调用afterPropertiesSet()方法，在方法中启动netty服务器
 * @author CRC
 * @create 2018-06-26 11:52
 **/
public class RpcServer implements ApplicationContextAware,InitializingBean {
    private Logger logger= LoggerFactory.getLogger(RpcServer.class);
    private Map<String,Object> handlerMap=new HashMap<String, Object>();

    private String serverAddress;//服务器地址
    private ServiceRegistry serviceRegistry;//注册服务

    public RpcServer(String serverAddress){
        //由用户在spring中注入服务器地址
        this.serverAddress=serverAddress;
    }

    public RpcServer(String serverAddress,ServiceRegistry serviceRegistry){
        this.serverAddress=serverAddress;
        this.serviceRegistry=serviceRegistry;
    }


    /*
        此方法会在spring构造实例时自动调用，在此方法中获取有自定义获取rpc服务注解的类
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(beans)){
            //获取业务接口的全名，才能反射调用
            for (Object obj : beans.values()) {
                String name = obj.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(name,obj);
            }
        }
    }
    /**
     * 在此启动netty服务，绑定handle流水线：
     * 1、接收请求数据进行反序列化得到request对象
     * 2、根据request中的参数，让RpcHandler从handlerMap中找到对应的业务imple，调用指定方法，获取返回结果
     * 3、将业务调用结果封装到response并序列化后发往客户端
     *
     */
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();//处理连接
        EventLoopGroup workGroup = new NioEventLoopGroup();//处理读写

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RpcDecoder(RpcRequest.class))//注册解码器==反序列化request对象==in1
                                 .addLast(new RpcEncoder(RpcResponse.class))//注册编码器，最后执行==out
                                 .addLast(new RpcHandler(handlerMap));//注册处理器==in2
                }
            }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);

            String[] array=serverAddress.split(":");//分割服务器地址
            if(array.length!=2){
                throw new ValidationException("传入的地址格式有误");
            }
            String host=array[0];//ip
            int port=Integer.parseInt(array[1]);//端口
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            logger.debug("rpc服务器启动在------------------------"+port);
            if(serviceRegistry!=null){
                //启动后注册到zookeeper中
                serviceRegistry.register(serverAddress);
            }
            future.channel().closeFuture().sync();
        }finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
