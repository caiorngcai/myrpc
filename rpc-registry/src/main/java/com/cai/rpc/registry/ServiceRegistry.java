package com.cai.rpc.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cai.rpc.registry.Contant;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 服务注册到zk上
 *
 * @author CRC
 * @create 2018-06-26 22:09
 **/
public class ServiceRegistry {
    private static final Logger LOGGER=LoggerFactory.getLogger(ServiceRegistry.class);
    private CountDownLatch latch=new CountDownLatch(1);//确保在连接zookeeper服务器成功之后其他操作操作才可以发生
    private String registryAdress;//服务器地址
    public ServiceRegistry(String registryAdress){
        this.registryAdress=registryAdress;
    }
    /*
        注册服务
     */
    public void register(String data) throws IOException, KeeperException, InterruptedException {
        if(data!=null){
            ZooKeeper zooKeeper=connectServer();
            if(zooKeeper!=null){
                createnNode(zooKeeper,data);//创建节点数据
            }
        }


    }

    /*
        连接zookeeper服务器
     */
    private ZooKeeper connectServer() throws IOException {
        ZooKeeper zooKeeper=null;
        try {
            zooKeeper=new ZooKeeper(registryAdress, Contant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState()==Event.KeeperState.SyncConnected){
                        latch.countDown();//已经连接上，锁数量减一
                    }
                }
            });
            latch.await();//未连接上，阻塞在这里
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }
    /*
        创建节点数据
     */
    private void createnNode(ZooKeeper zooKeeper, String data) throws KeeperException, InterruptedException {
        try {
            byte[] bytes = data.getBytes();
            if(zooKeeper.exists(Contant.ZK_REGISTRY_PATH,null)==null){
                //路径不存在，先创建一个
                zooKeeper.create(Contant.ZK_REGISTRY_PATH,null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            String path=zooKeeper.create(Contant.ZK_DATA_PATH,bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("服务注册成功，节点数据为为-----"+path+"---------"+data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
