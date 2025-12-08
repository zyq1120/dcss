package cn.masu.dcs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 校对详情VO
 *
 * @author zyq
 * @since 2025-12-07
 */
@Data
public class ReviewDetailVO {

    /**
     * 文件信息
     */
    private FileInfo fileInfo;

    /**
     * 提取信息
     */
    private ExtractInfo extractInfo;

    /**
     * 字段列表
     */
    private List<FieldInfo> fields;

    /**
     * 表格数据（如课程成绩）
     */
    private List<Map<String, Object>> tableData;

    /**
     * 文件信息
     */
    @Data
    public static class FileInfo {
        /**
         * 文件ID
         */
        private Long id;

        /**
         * 文件名
         */
        private String fileName;

        /**
         * 文件类型
         */
        private String fileType;

        /**
         * 文件大小
         */
        private Long fileSize;

        /**
         * 文件大小（格式化）
         */
        private String fileSizeFormatted;

        /**
         * MinIO预览URL
         */
        private String previewUrl;

        /**
         * 缩略图URL
         */
        private String thumbnailUrl;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 提取信息
     */
    @Data
    public static class ExtractInfo {
        /**
         * 提取主表ID
         */
        private Long id;

        /**
         * 姓名
         */
        private String ownerName;

        /**
         * 学号/证件号
         */
        private String ownerId;

        /**
         * 整体置信度
         */
        private Double confidence;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 状态名称
         */
        private String statusName;
    }

    /**
     * 字段信息
     */
    @Data
    public static class FieldInfo {
        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 字段标签
         */
        private String fieldLabel;

        /**
         * 字段值
         */
        private Object fieldValue;

        /**
         * 原始值（AI识别的）
         */
        private Object originalValue;

        /**
         * 字段类型 (text/number/date/table)
         */
        private String fieldType;

        /**
         * 置信度
         */
        private Double confidence;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 是否关键字段
         */
        private Boolean keyField;

        /**
         * 是否已修改
         */
        private Boolean modified;

        /**
         * 修改人
         */
        private String modifier;

        /**
         * 修改时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime modifyTime;
    }
}

