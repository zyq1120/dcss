package cn.masu.dcs.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传DTO
 * @author System
 */
@Data
public class FileUploadDTO {
    private MultipartFile file;
    private Long templateId;
    private String description;
}

