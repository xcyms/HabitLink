package org.xcyms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xcyms.common.ApiResult;
import org.xcyms.common.Constant;
import org.xcyms.common.annotation.ApiDoc;
import org.xcyms.common.annotation.Log;
import org.xcyms.entity.File;
import org.xcyms.entity.dto.FileDTO;
import org.xcyms.service.IFileService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@ApiDoc("文件管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final IFileService fileService;

    @Log("上传文件")
    @ApiDoc("单文件上传接口")
    @PostMapping("/upload")
    public ApiResult<FileDTO> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "businessId", required = false) Long businessId,
            @RequestParam(value = "businessType", required = false, defaultValue = "file") String businessType
    ) {
        return fileService.upload(file, businessId, businessType);
    }

    @ApiDoc(value = "获取文件列表", notes = "获取当前用户可访问的文件列表", order = 1)
    @GetMapping("/list")
    public ApiResult<List<File>> getList() {
        return ApiResult.success(fileService.getAccessibleList());
    }

    @ApiDoc(value = "分页查询文件", notes = "分页查看文件列表", order = 2)
    @PostMapping("/page")
    public ApiResult<IPage<FileDTO>> getPage(Page<File> page, @RequestBody FileDTO fileDTO) {
        return ApiResult.success(fileService.getPage(page, fileDTO));
    }

    @ApiDoc(value = "获取文件详情", notes = "根据ID获取当前用户可访问的文件详情", order = 3)
    @GetMapping("/{id}")
    public ApiResult<File> get(@PathVariable Long id) {
        File file = fileService.getAccessibleById(id);
        if (file == null) {
            return ApiResult.error(404, "文件不存在或无权访问");
        }
        return ApiResult.success(file);
    }

    @Log("删除文件")
    @ApiDoc(value = "删除文件", notes = "根据ID删除文件", order = 4)
    @DeleteMapping("/{id}")
    public ApiResult<String> delete(@PathVariable Long id) {
        return fileService.delete(id);
    }

    @Log("批量删除文件")
    @ApiDoc(value = "批量删除文件", notes = "批量删除文件", order = 5)
    @DeleteMapping("/batch")
    public ApiResult<String> deleteBatch(@RequestBody Long[] ids) {
        return fileService.deleteBatch(ids);
    }

    @Log("下载文件")
    @ApiDoc(value = "下载文件", notes = "根据ID下载文件", order = 6)
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
        File file = fileService.getAccessibleById(id);
        if (file == null) {
            log.warn("文件不存在或无权访问, id={}", id);
            return ResponseEntity.notFound().build();
        }

        String rootPath = fileService.getConfigValue(file.getUserId(), Constant.ConfigKey.UPLOAD_PATH);
        String filePath = resolveFilePath(file.getUrl());
        if (filePath == null) {
            log.error("未知的文件URL格式: url={}", file.getUrl());
            return ResponseEntity.badRequest().build();
        }

        Path path = Paths.get(rootPath, filePath);
        if (!Files.exists(path)) {
            log.error("文件不存在, path={}", path);
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(path);
        String encodedFileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(encodedFileName, StandardCharsets.UTF_8)
                .build());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(bytes);
    }

    /**
     * 统一把数据库中的 URL 转成上传根目录下的相对路径。
     */
    private String resolveFilePath(String url) {
        if (url == null) {
            return null;
        }

        String filePath = url;
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int pathStart = url.indexOf("/", url.indexOf("://") + 3);
            if (pathStart <= 0) {
                return null;
            }
            filePath = url.substring(pathStart);
        }

        if (!filePath.startsWith(Constant.UPLOAD_ROOT_PATH)) {
            return null;
        }

        return filePath.substring(Constant.UPLOAD_ROOT_PATH.length());
    }
}
