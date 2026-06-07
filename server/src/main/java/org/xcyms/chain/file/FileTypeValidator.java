package org.xcyms.chain.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.service.IConfigService;

import java.util.Set;

/**
 * <p>
 * 文件格式验证器
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileTypeValidator implements FileValidator {

    private final IConfigService configService;

    @Override
    public ApiResult<Void> validate(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return ApiResult.error("文件名不能为空");
        }

        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String allowedExtStr = configService.getConfigValue(userId, Constant.ConfigKey.ALLOWED_EXTENSIONS);
        Set<String> allowedExtensions = StringUtils.isNotBlank(allowedExtStr)
                ? Set.of(allowedExtStr.toLowerCase().split(","))
                : Set.of("jpg", "jpeg", "png", "gif", "webp");

        String ext = suffix.startsWith(".") ? suffix.substring(1) : suffix;
        if (!allowedExtensions.contains(ext)) {
            return ApiResult.error("不支持的文件格式，允许的格式: " + String.join(", ", allowedExtensions));
        }
        return ApiResult.success();
    }

    @Override
    public String getValidatorName() {
        return "文件格式验证";
    }
}
