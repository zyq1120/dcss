package cn.masu.dcs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.masu.dcs.entity.AiTrainingSample;
import cn.masu.dcs.service.AiTrainingSampleService;
import cn.masu.dcs.mapper.AiTrainingSampleMapper;
import org.springframework.stereotype.Service;

/**
* @author zyq
* @description 针对表【ai_training_sample(AI负样本/训练数据表)】的数据库操作Service实现
* @createDate 2025-11-30 11:18:49
*/
@Service
public class AiTrainingSampleServiceImpl extends ServiceImpl<AiTrainingSampleMapper, AiTrainingSample>
    implements AiTrainingSampleService{

}




