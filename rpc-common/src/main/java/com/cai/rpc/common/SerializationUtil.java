package com.cai.rpc.common;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化工具，用于转化对象和字节数据，基于protostuff
 *
 * @author CRC
 * @create 2018-06-26 20:19
 **/
public class SerializationUtil {
    private static Map<Class<?>,Schema<?>> cachedASchema=new ConcurrentHashMap<Class<?>, Schema<?>>();
    //Objenesis是一个轻量级的Java库，作用是绕过构造器创建一个实例。
    private static Objenesis objenesis=new ObjenesisStd(true);
    //空参构造
    private SerializationUtil(){

    }
    /*
        获取类的schema
     */

    public static <T> Schema<T> getSchema(Class<T> tClass){
        Schema<T> schema = (Schema<T>) cachedASchema.get(tClass);
        if(schema==null){
             schema = RuntimeSchema.createFrom(tClass);
            if(schema!=null){
                //放到缓存
                cachedASchema.put(tClass,schema);
            }
        }
        return schema;
    }
    /*
        序列化，对象--字节数组
     */
    public static <T> byte[] serialize(T obj){
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj,schema,linkedBuffer);
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }finally {
            linkedBuffer.clear();
        }
    }

    /*
        反序列化，字节数组--对象
     */
    public static <T> T deserialize(byte[] data,Class<T> tClass){
        try {
            T instance = objenesis.newInstance(tClass);//实例化
            Schema<T> schema = getSchema(tClass);
            ProtostuffIOUtil.mergeFrom(data,instance,schema);
            return instance;
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(),e);
        }
    }

}
