package com.cai.rpc.registry;

/**
 * zookeeper服务中需要用到的常量
 *
 * @author CRC
 * @create 2018-06-27 9:13
 **/
public class Contant {
    public static final int ZK_SESSION_TIMEOUT=5000;//超时时间设置
    public static final String ZK_REGISTRY_PATH="/registry/";//注册节点信息
    public static final String ZK_DATA_PATH=ZK_REGISTRY_PATH+"/data";//节点
}
