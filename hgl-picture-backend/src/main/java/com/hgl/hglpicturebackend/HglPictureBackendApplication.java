package com.hgl.hglpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author 请别把我整破防
 */
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
@EnableAsync
@MapperScan("com.hgl.hglpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class HglPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HglPictureBackendApplication.class, args);
    }

}
