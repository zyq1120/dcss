package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.FileUpdateStatusDTO;
import cn.masu.dcs.service.FileService;
import cn.masu.dcs.vo.FileDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 * <p>
 * 同时支持 /api/file 和 /api/files 路径，保证向后兼容
 * </p>
 * @author zyq
 */
@RestController
@RequestMapping({"/api/file", "/api/files"})
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<Long> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long templateId,
            @RequestParam Long userId) {
        Long id = fileService.uploadFile(file, templateId, userId);
        return R.ok("上传成功", id);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        FileDetailVO fileDetail = fileService.getFileDetail(id);
        byte[] fileContent = fileService.downloadFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileDetail.getFileName() + "\"")
                .body(fileContent);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteFile(@PathVariable Long id) {
        Boolean result = fileService.deleteFile(id);
        return R.ok("删除成功", result);
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{id}")
    public R<FileDetailVO> getFileDetail(@PathVariable Long id) {
        FileDetailVO vo = fileService.getFileDetail(id);
        return R.ok(vo);
    }

    /**
     * 更新文件状态
     */
    @PutMapping("/status")
    public R<Boolean> updateFileStatus(@Validated @RequestBody FileUpdateStatusDTO dto) {
        Boolean result = fileService.updateFileStatus(dto);
        return R.ok("状态更新成功", result);
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/page")
    public R<PageResult<FileDetailVO>> getFilePage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {
        PageResult<FileDetailVO> pageResult = fileService.getFilePage(current, size, keyword, status, userId);
        return R.ok(pageResult);
    }
}

