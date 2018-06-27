package com.cai.rpc.client;

import com.cai.rpc.common.RpcDecoder;
import com.cai.rpc.common.RpcEncoder;
import com.cai.rpc.common.RpcRequest;
import com.cai.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPC服务的客户端
 *
 * @author CRC
 * @create 2018-06-27 14:01
 **/
public class RpcClient extends SimpleChannelInboundHandler{
    private static final Logger LOGGER= LoggerFactory.getLogger(RpcClient.class);
    private String host;
    private int port;

    private RpcResponse response;
    private final Object object=new Object();

    public RpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }

    public RpcResponse send(RpcRequest request) throws Exception{
        EventLoopGroup group=new NioEventLoopGroup();
        try {
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RpcEncoder(RpcRequest.class))//OUT1
                                .addLast(new RpcDecoder(RpcResponse.class))//IN1
                                .addLast(RpcClient.this);//IN2
                    }
            }).option(ChannelOption.SO_KEEPALIVE,true);
            //连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //将request对象写入outbundle处理后发出（即RpcEncoder编码器）
            future.channel().writeAndFlush(request).sync();

            // 用线程等待的方式决定是否关闭连接
            // 其意义是：先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (object){
                object.wait();
            }
            if(response!=null){
                future.channel().closeFuture().sync();
            }
            return response;
        }finally {
            group.shutdownGracefully();
        }
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.response= response;
        synchronized (object){
            object.notifyAll();
        }

    }
    //异常处理

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        LOGGER.error("RPC框架客户端发生异常");
        ctx.close();
    }

}
