package org.xcyms.strategy.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * <p>
 * 七牛云存储策略实现
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
public class QiniuStorageStrategy implements StorageStrategy {

    @Override
    public String upload(File file, String relativePath) {
        log.info("使用七牛云存储上传文件: {}", relativePath);
        throw new UnsupportedOperationException("七牛云存储暂未实现，请配置相关依赖和参数");
    }

    @Override
    public void delete(String url) {
        log.info("使用七牛云存储删除文件: {}", url);
        throw new UnsupportedOperationException("七牛云存储暂未实现，请配置相关依赖和参数");
    }

    @Override
    public String getStorageType() {
        return "qiniu";
    }
}
