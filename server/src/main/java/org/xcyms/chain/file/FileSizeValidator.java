package org.xcyms.chain.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;

/**
 * <p>
 * 文件大小验证器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSizeValidator implements FileValidator {

    private final IConfigService configService;

    @Override
    public ApiResult<Void> validate(MultipartFile file, Long userId) {
        String maxSizeStr = configService.getConfigValue(userId, Constant.ConfigKey.MAX_FILE_SIZE);
        long maxSize = StringUtils.isNotBlank(maxSizeStr) ? Long.parseLong(maxSizeStr) : 10 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            return ApiResult.error("文件大小超限，最大允许 " + (maxSize / 1024 / 1024) + "MB");
        }
        return ApiResult.success();
    }

    @Override
    public String getValidatorName() {
        return "文件大小验证";
    }
}
