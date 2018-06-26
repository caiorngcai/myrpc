package com.cai.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器，把输入流转化成对象
 *
 * @author CRC
 * @create 2018-06-26 21:17
 **/
public class RpcDecoder extends ByteToMessageDecoder{
    private Class<?> genericClass;
    public RpcDecoder(Class<?> genericClass){
        this.genericClass=genericClass;
    }

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if(byteBuf.readableBytes()<4){
            return;
        }
        byteBuf.markReaderIndex();//标记到当前位置
        int dataLenght=byteBuf.readInt();//输入流长度
        if(dataLenght<0){
            channelHandlerContext.close();
        }
        if(byteBuf.readableBytes()<dataLenght){
            byteBuf.resetReaderIndex();//重新定位到标记处
        }
        byte[] data=new byte[dataLenght];
        byteBuf.readBytes(data);//读到数组
        Object obj = SerializationUtil.deserialize(data, genericClass);
        list.add(obj);

    }
}
