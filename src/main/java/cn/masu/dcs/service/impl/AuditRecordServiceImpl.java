package cn.masu.dcs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.masu.dcs.entity.AuditRecord;
import cn.masu.dcs.service.AuditRecordService;
import cn.masu.dcs.mapper.AuditRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author zyq
* @description 针对表【audit_record(人工审核/校对记录)】的数据库操作Service实现
* @createDate 2025-11-30 11:18:59
*/
@Service
public class AuditRecordServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord>
    implements AuditRecordService{

}




