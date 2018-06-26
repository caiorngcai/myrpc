package com.cai.rpc.server;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * rpc服务器主类
 *
 * @author CRC
 * @create 2018-06-26 11:52
 **/
public class RpcServer implements ApplicationContextAware,InitializingBean {


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {


    }
    public void afterPropertiesSet() throws Exception {

    }
}
