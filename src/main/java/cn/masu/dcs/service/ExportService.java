package cn.masu.dcs.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * 导出服务接口
 *
 * @author zyq
 * @since 2025-12-07
 */
public interface ExportService {

    /**
     * 导出文件列表
     *
     * @param status 状态
     * @param keyword 关键字
     * @return Excel输出流
     */
    ByteArrayOutputStream exportFiles(Integer status, String keyword);

    /**
     * 导出审核记录
     *
     * @param fileId 文件ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Excel输出流
     */
    ByteArrayOutputStream exportAuditRecords(Long fileId, String startDate, String endDate);

    /**
     * 导出统计报表
     *
     * @param type 报表类型
     * @return Excel输出流
     */
    ByteArrayOutputStream exportReport(String type);

    /**
     * 批量导出文件数据
     *
     * @param fileIds 文件ID列表
     * @return Excel输出流
     */
    ByteArrayOutputStream batchExportFileData(List<Long> fileIds);
}

