package cn.masu.dcs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.service.SysUserService;
import cn.masu.dcs.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author zyq
* @description 针对表【sys_user(用户表)】的数据库操作Service实现
* @createDate 2025-11-30 11:20:13
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService{

}




