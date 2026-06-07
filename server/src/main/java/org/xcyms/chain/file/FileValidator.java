package org.xcyms.chain.file;

import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;

/**
 * <p>
 * 文件验证器接口
 * </p>
 *
 * @author liu-xu
 * @since 2026-05-11
 */
public interface FileValidator {

    /**
     * 验证文件
     *
     * @param file   上传文件
     * @param userId 用户ID
     * @return org.xcyms.common.ApiResult<java.lang.Void>
     * @author liu-xu
     * @date 2026/05/11
     */
    ApiResult<Void> validate(MultipartFile file, Long userId);

    /**
     * 获取验证器名称
     *
     * @return java.lang.String
     * @author liu-xu
     * @date 2026/05/11
     */
    String getValidatorName();
}
