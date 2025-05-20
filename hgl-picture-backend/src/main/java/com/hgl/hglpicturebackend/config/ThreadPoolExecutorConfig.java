package com.hgl.hglpicturebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ClassName: ThreadPoolExecutorConfig
 * Package: com.hgl.hglpicturebackend.config
 * Description:
 *
 * @Author HGL
 * @Create: 2025/4/8 8:37
 */
@Component
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        // 获取CPU核数
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        return new ThreadPoolExecutor(
                availableProcessors,
                2*availableProcessors,
                60,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.ArrayBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
