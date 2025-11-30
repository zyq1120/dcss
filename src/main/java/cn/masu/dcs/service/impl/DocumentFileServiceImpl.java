package cn.masu.dcs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.service.DocumentFileService;
import cn.masu.dcs.mapper.DocumentFileMapper;
import org.springframework.stereotype.Service;

/**
* @author zyq
* @description 针对表【document_file(教务材料文件记录表)】的数据库操作Service实现
* @createDate 2025-11-30 11:19:34
*/
@Service
public class DocumentFileServiceImpl extends ServiceImpl<DocumentFileMapper, DocumentFile>
    implements DocumentFileService{

}




