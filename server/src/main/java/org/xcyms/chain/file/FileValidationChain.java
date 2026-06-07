package org.xcyms.chain.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;

import java.util.List;

/**
 * <p>
 * 文件验证链
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidationChain {

    private final List<FileValidator> validators;

    /**
     * 执行文件验证链
     *
     * @param file   上传文件
     * @param userId 用户ID
     * @return org.xcyms.common.ApiResult<java.lang.Void>
     * @author liu-xu
     * @date 2026/05/11
     */
    public ApiResult<Void> validate(MultipartFile file, Long userId) {
        for (FileValidator validator : validators) {
            try {
                ApiResult<Void> result = validator.validate(file, userId);
                if (result.getCode() != 200) {
                    log.warn("文件验证失败: {}, 用户ID: {}", validator.getValidatorName(), userId);
                    return result;
                }
            } catch (Exception e) {
                log.error("文件验证异常: {}, 用户ID: {}", validator.getValidatorName(), userId, e);
                return ApiResult.error("文件验证失败: " + e.getMessage());
            }
        }
        return ApiResult.success();
    }
}
