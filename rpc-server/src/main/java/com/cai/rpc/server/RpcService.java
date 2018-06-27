package com.cai.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义RPC服务注解
 */
@Target({ElementType.TYPE})//作用在接口
@Retention(RetentionPolicy.RUNTIME)//运行时也有效，这样才能通过反射机制读取注解信息
@Component//使能够被spring注解扫描
public @interface RpcService {
    Class<?> value();
}
