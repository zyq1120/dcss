package cn.masu.dcs.service.impl;

import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.service.AuthService;
import cn.masu.dcs.vo.UserInfoVO;
import cn.masu.dcs.common.config.GlobalExceptionHandler.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.JwtUtils;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.service.UserService;
import cn.masu.dcs.dto.UserCreateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userService.getUserByUsername(request.getUsername());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public LoginResponse register(UserCreateDTO request) {
        // 使用 UserService 创建用户
        Long userId = userService.createUser(request);
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public void logout(Long userId) {
        userService.invalidateToken(userId);
        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    public String refreshToken(String oldToken) {
        if (!jwtUtils.validateToken(oldToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtils.getUserIdFromToken(oldToken);
        String username = jwtUtils.getUsernameFromToken(oldToken);
        Integer tokenVersion = jwtUtils.getTokenVersionFromToken(oldToken);

        SysUser user = userService.getById(userId);
        if (user == null || !user.getTokenVersion().equals(tokenVersion)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        return jwtUtils.generateToken(userId, username, tokenVersion);
    }
}
