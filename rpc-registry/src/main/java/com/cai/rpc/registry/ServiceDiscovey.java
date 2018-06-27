package com.cai.rpc.registry;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 用户客户端服务发现，负载均衡
 *
 * @author CRC
 * @create 2018-06-27 9:40
 **/
public class ServiceDiscovey {
    private static final Logger LOGGER= LoggerFactory.getLogger(ServiceRegistry.class);
    private CountDownLatch latch=new CountDownLatch(1);
    private volatile List<String> dataList=new ArrayList<String>();//保证可见性
    private String registryAddress;

    /*
        客户端服务发现
     */
    public ServiceDiscovey(String registryAddress) throws IOException {
        this.registryAddress=registryAddress;

        ZooKeeper zooKeeper=connectServer();//连接服务器
        if(zooKeeper!=null){
            watchNode(zooKeeper);//监听服务器的节点变化
        }
    }

    /*
        框架客户端发现可用的服务器地址
     */
    public String discover(){
        int size = dataList.size();
        if(size>0){
            if(size==1){
                return dataList.get(0);//只有一个服务器
            }else {
                return dataList.get(ThreadLocalRandom.current().nextInt(size));//多线程环境下减少竞争，更加快
            }
        }
        return null;
    }
    /*
        连接服务器
     */
    private ZooKeeper connectServer() throws IOException {
        ZooKeeper zooKeeper=null;
        try {
            zooKeeper=new ZooKeeper(registryAddress, Contant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

    /*
        监听节点的变化
     */
    private void watchNode(final ZooKeeper zooKeeper)  {
        try {
            List<String> nodeList=zooKeeper.getChildren(Contant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if(Event.EventType.NodeChildrenChanged == watchedEvent.getType()){
                        watchNode(zooKeeper);//重新注册监听子节点变化事件，并再次获取变化后的节点状态
                    }
                }
            });
            List<String> dataList=new ArrayList<String>();
            for (String node : nodeList) {
                byte[] data = zooKeeper.getData(Contant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(data));
            }
            LOGGER.debug("获取服务器节点信息成功");
            this.dataList=dataList;//向外暴露信息
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
