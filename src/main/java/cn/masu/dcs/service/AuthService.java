package cn.masu.dcs.service;

import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.vo.UserInfoVO;

/**
 * 认证服务接口
 * @author System
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    LoginResponse register(cn.masu.dcs.dto.UserCreateDTO request);

    /**
     * 用户登出
     */
    void logout(Long userId);

    /**
     * 获取当前用户信息
     */
    UserInfoVO getCurrentUserInfo(Long userId);

    /**
     * 刷新Token
     */
    String refreshToken(String oldToken);
}
