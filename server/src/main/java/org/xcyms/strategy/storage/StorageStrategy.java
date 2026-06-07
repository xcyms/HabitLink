package org.xcyms.strategy.storage;

import java.io.File;

/**
 * <p>
 * 文件存储策略接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public interface StorageStrategy {

    String upload(File file, String relativePath);

    void delete(String url);

    String getStorageType();
}
