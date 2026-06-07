package org.xcyms.strategy.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;
import org.xcyms.utils.IdGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 * 本地文件存储策略实现
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalStorageStrategy implements StorageStrategy {

    private final IConfigService configService;

    @Override
    public String upload(File file, String relativePath) {
        String rootPath = configService.getConfigValue(null, Constant.ConfigKey.UPLOAD_PATH);
        File destFile = new File(rootPath, relativePath.replace("/", File.separator));

        if (!destFile.getParentFile().exists()) {
            if (!destFile.getParentFile().mkdirs()) {
                throw new RuntimeException("创建目录失败");
            }
        }

        try {
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return Constant.UPLOAD_ROOT_PATH + relativePath;
        } catch (IOException e) {
            log.error("本地存储上传失败", e);
            throw new RuntimeException("文件保存失败");
        }
    }

    @Override
    public void delete(String url) {
        String rootPath = configService.getConfigValue(null, Constant.ConfigKey.UPLOAD_PATH);
        if (url.startsWith(Constant.UPLOAD_ROOT_PATH)) {
            String relativePath = url.replace(Constant.UPLOAD_ROOT_PATH, "");
            File file = new File(rootPath, relativePath.replace("/", File.separator));
            if (file.exists()) {
                if (!file.delete()) {
                    throw new RuntimeException("文件删除失败");
                }
            }
        }
    }

    @Override
    public String getStorageType() {
        return "local";
    }

    /**
     * 生成文件名
     *
     * @param originalFilename 原始文件名
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    public String generateFileName(String originalFilename) {
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        return IdGenerator.nextIdStr() + suffix;
    }

    /**
     * 构建相对路径
     *
     * @param userId   用户ID
     * @param category 业务类型
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    public String buildRelativePath(Long userId, String category) {
        String userSubDir = configService.getConfigValue(userId, Constant.ConfigKey.USER_UPLOAD_DIR);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        StringBuilder sb = new StringBuilder();
        if (userSubDir != null && !userSubDir.isEmpty()) {
            sb.append(userSubDir).append("/");
        }
        sb.append(category).append("/").append(datePath).append("/");
        return sb.toString();
    }
}
