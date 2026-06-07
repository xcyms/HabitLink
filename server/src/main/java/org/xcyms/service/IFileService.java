package org.xcyms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.entity.File;
import org.xcyms.entity.dto.FileDTO;

import java.util.List;

public interface IFileService extends IService<File> {

    /**
     * 上传单个文件并返回存储后的文件信息。
     */
    ApiResult<FileDTO> upload(MultipartFile file, Long businessId, String businessType);

    /**
     * 按当前登录用户的可见范围分页查询文件。
     */
    IPage<FileDTO> getPage(Page<File> page, FileDTO fileDTO);

    ApiResult<String> delete(Long id);

    ApiResult<String> deleteBatch(Long[] ids);

    /**
     * 获取当前登录用户可访问的文件列表。
     */
    List<File> getAccessibleList();

    /**
     * 获取当前登录用户可访问的指定文件。
     */
    File getAccessibleById(Long id);

    String getConfigValue(Long userId, String configKey);
}
