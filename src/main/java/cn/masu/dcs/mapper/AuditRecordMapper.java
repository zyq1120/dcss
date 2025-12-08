package cn.masu.dcs.mapper;

import cn.masu.dcs.entity.AuditRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核记录Mapper
 * @author System
 */
@Mapper
public interface AuditRecordMapper extends BaseMapper<AuditRecord> {

}

