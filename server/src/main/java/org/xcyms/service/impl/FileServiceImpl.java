package org.xcyms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.chain.file.FileValidationChain;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.common.enums.YesNoEnum;
import org.xcyms.entity.File;
import org.xcyms.entity.dto.FileDTO;
import org.xcyms.mapper.FileMapper;
import org.xcyms.service.IConfigService;
import org.xcyms.service.IFileService;
import org.xcyms.strategy.storage.LocalStorageStrategy;
import org.xcyms.strategy.storage.StorageStrategy;
import org.xcyms.strategy.storage.StorageStrategyFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    private final IConfigService configService;
    private final ModelMapper mapper;
    private final StorageStrategyFactory storageStrategyFactory;
    private final LocalStorageStrategy localStorageStrategy;
    private final FileValidationChain fileValidationChain;

    @Override
    @CacheEvict(value = Constant.Cache.STATS, key = "'dashboard'")
    public ApiResult<FileDTO> upload(MultipartFile file, Long businessId, String businessType) {
        if (file == null || file.isEmpty()) {
            return ApiResult.error("文件不能为空");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        try {
            return processSingleFile(file, userId, businessId, businessType);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ApiResult.error("文件上传失败：" + e.getMessage());
        }
    }

    private ApiResult<FileDTO> processSingleFile(
            MultipartFile file,
            Long userId,
            Long businessId,
            String businessType
    ) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        ApiResult<Void> validateResult = fileValidationChain.validate(file, userId);
        if (validateResult.getCode() != 200) {
            return ApiResult.error(validateResult.getMessage());
        }

        String storageType = configService.getConfigValue(userId, Constant.ConfigKey.STORAGE_TYPE);
        StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        String relativePath = localStorageStrategy.buildRelativePath(userId, businessType);
        String newFileName = localStorageStrategy.generateFileName(originalFilename);
        String finalRelativePath = relativePath + newFileName;

        java.io.File tempFile = null;
        try {
            tempFile = java.io.File.createTempFile("upload_", suffix);
            file.transferTo(tempFile);

            String webUrl = strategy.upload(tempFile, finalRelativePath);

            File fileEntity = new File();
            fileEntity.setUserId(userId);
            fileEntity.setBusinessId(businessId);
            fileEntity.setBusinessType(businessType);
            fileEntity.setName(originalFilename);
            fileEntity.setUrl(webUrl);
            fileEntity.setSize(file.getSize());
            fileEntity.setType(suffix.substring(1));
            fileEntity.setDeleted(YesNoEnum.NO);
            this.save(fileEntity);

            return ApiResult.success(mapper.map(fileEntity, FileDTO.class));
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
            }
        }
    }

    @Override
    public IPage<FileDTO> getPage(Page<File> page, FileDTO fileDTO) {
        // 非管理员只能看到自己的文件，避免文件列表越权。
        LambdaQueryWrapper<File> wrapper = new QueryWrapper<File>().lambda()
                .like(fileDTO != null && StringUtils.isNotBlank(fileDTO.getName()), File::getName, fileDTO != null ? fileDTO.getName() : null)
                .eq(fileDTO != null && StringUtils.isNotBlank(fileDTO.getType()), File::getType, fileDTO != null ? fileDTO.getType() : null)
                .orderByDesc(File::getCreateTime);

        if (!isAdmin()) {
            wrapper.eq(File::getUserId, StpUtil.getLoginIdAsLong());
        }

        return this.page(page, wrapper).convert(file -> mapper.map(file, FileDTO.class));
    }

    @Override
    @CacheEvict(value = Constant.Cache.STATS, key = "'dashboard'")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> delete(Long id) {
        File file = this.getById(id);
        if (file == null) {
            return ApiResult.error("文件不存在");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        if (!canAccessFile(file, userId)) {
            return ApiResult.error("无删除权限");
        }

        try {
            String storageType = configService.getConfigValue(file.getUserId(), Constant.ConfigKey.STORAGE_TYPE);
            StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
            strategy.delete(file.getUrl());
            this.removeById(id);
            return ApiResult.success("删除成功");
        } catch (Exception e) {
            log.error("文件删除失败, id={}", id, e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = Constant.Cache.STATS, key = "'dashboard'")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> deleteBatch(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return ApiResult.error("请选择要删除的文件");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        List<File> fileList = this.listByIds(List.of(ids));

        for (File file : fileList) {
            if (!canAccessFile(file, userId)) {
                return ApiResult.error("部分文件无删除权限");
            }
        }

        // 每个文件按所属用户的存储配置删除，避免管理员批量删除时走错存储桶或目录。
        int deletedCount = 0;
        List<String> failedUrls = new ArrayList<>();
        for (File file : fileList) {
            if (file == null) {
                continue;
            }
            try {
                String storageType = configService.getConfigValue(file.getUserId(), Constant.ConfigKey.STORAGE_TYPE);
                StorageStrategy strategy = storageStrategyFactory.getStrategy(storageType);
                strategy.delete(file.getUrl());
                this.removeById(file.getId());
                deletedCount++;
            } catch (Exception e) {
                failedUrls.add(file.getUrl());
                log.error("文件删除失败, id={}", file.getId(), e);
            }
        }

        if (deletedCount == ids.length) {
            return ApiResult.success("批量删除成功");
        }
        if (!failedUrls.isEmpty()) {
            log.warn("部分文件删除失败, urls={}", failedUrls);
        }
        return ApiResult.error("部分文件删除失败");
    }

    @Override
    public List<File> getAccessibleList() {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<File>()
                .orderByDesc(File::getCreateTime);
        if (!isAdmin()) {
            wrapper.eq(File::getUserId, StpUtil.getLoginIdAsLong());
        }
        return this.list(wrapper);
    }

    @Override
    public File getAccessibleById(Long id) {
        File file = this.getById(id);
        if (file == null) {
            return null;
        }
        return canAccessFile(file, StpUtil.getLoginIdAsLong()) ? file : null;
    }

    /**
     * 管理员可访问全部文件，普通用户仅可访问自己的文件。
     */
    private boolean canAccessFile(File file, Long userId) {
        return file != null && (isAdmin() || userId.equals(file.getUserId()));
    }

    private boolean isAdmin() {
        return StpUtil.hasRole("ADMIN");
    }

    @Override
    public String getConfigValue(Long userId, String configKey) {
        return configService.getConfigValue(userId, configKey);
    }
}
