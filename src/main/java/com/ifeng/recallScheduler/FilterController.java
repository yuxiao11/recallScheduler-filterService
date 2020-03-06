package com.ifeng.recallScheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * 召回调度模块启动类
 */
@EnableHystrix //开启Hystrix的熔断机制
@EnableEurekaClient //将此服务注册到Eureka 服务注册中心
@SpringBootApplication
public class FilterController {

    public static void main(String args){SpringApplication.run(FilterController.class, args);}
}
