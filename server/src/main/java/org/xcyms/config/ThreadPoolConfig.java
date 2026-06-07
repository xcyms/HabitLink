package org.xcyms.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>
 * 线程池配置类
 * </p>
 *
 * @author liu-xu
 * @date 2026年01月21日 17:22
 */

@Configuration
public class ThreadPoolConfig {

    private final int cores = Runtime.getRuntime().availableProcessors();

    /**
     * 通用异步任务线程池 (默认池)
     * 适用于一般的后台逻辑、消息发送等
     */
    @Bean("commonExecutor")
    @Primary
    public Executor commonExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("common-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * I/O 密集型任务线程池
     * 适用于文件解析（如 EXIF）、外部 API 调用、大批量数据库操作
     */
    @Bean("ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // I/O 密集型核心线程数通常设置得更高
        executor.setCorePoolSize(cores * 2);
        executor.setMaxPoolSize(cores * 4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("io-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
