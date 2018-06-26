package com.cai.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码器，把输出流编码后写出
 *
 * @author CRC
 * @create 2018-06-26 21:36
 **/
public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> genericClass;
    public RpcEncoder(Class<?> genericClass){
        this.genericClass=genericClass;
    }

    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            //实例一致才能序列化
            byte[] bytes = SerializationUtil.serialize(msg);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }
}
