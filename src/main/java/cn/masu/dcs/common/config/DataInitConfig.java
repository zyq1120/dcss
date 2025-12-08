package cn.masu.dcs.common.config;

import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.entity.SysRole;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.entity.SysUserRole;
import cn.masu.dcs.mapper.SysRoleMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import cn.masu.dcs.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

/**
 * 数据初始化配置
 * <p>
 * 在应用启动时自动初始化超级管理员账户和基础角色
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitConfig {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 数据初始化Runner
     * <p>
     * 在Spring Boot应用启动完成后自动执行
     * </p>
     *
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("========================================");
            log.info("开始检查系统初始化状态");
            log.info("========================================");
            
            // 初始化超级管理员
            initSuperAdmin();
            
            // 初始化基础角色
            initBasicRoles();
            
            log.info("========================================");
            log.info("系统初始化检查完成");
            log.info("========================================");
        };
    }

    /**
     * 初始化超级管理员
     */
    private void initSuperAdmin() {
        // 检查是否已存在
        SysUser existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );
        
        if (existingUser != null) {
            log.info("✅ 超级管理员已存在: username={}", existingUser.getUsername());
            return;
        }

        log.info("🔧 检测到超级管理员不存在，开始创建...");

        try {
            // 1. 创建或获取超级管理员角色
            SysRole adminRole = createRoleIfNotExists("超级管理员", "SUPER_ADMIN");
            
            // 2. 创建超级管理员用户
            SysUser adminUser = new SysUser();
            adminUser.setId(idGenerator.nextId());
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setNickname("超级管理员");
            adminUser.setEmail("admin@system.com");
            adminUser.setPhone("13800138000");
            adminUser.setStatus(1); // 启用
            adminUser.setDeleted(0); // 未删除
            adminUser.setTokenVersion(0);
            adminUser.setVersion(0);
            adminUser.setCreateTime(new Date());
            adminUser.setUpdateTime(new Date());
            
            userMapper.insert(adminUser);
            log.info("✅ 超级管理员用户创建成功");
            log.info("   用户ID: {}", adminUser.getId());
            log.info("   用户名: admin");
            log.info("   初始密码: admin123");

            // 3. 分配超级管理员角色
            SysUserRole userRole = new SysUserRole();
            userRole.setId(idGenerator.nextId());
            userRole.setUserId(adminUser.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleMapper.insert(userRole);
            
            log.info("✅ 角色分配成功: userId={}, roleId={}", adminUser.getId(), adminRole.getId());
            log.info("========================================");
            log.info("🎉 超级管理员初始化完成！");
            log.info("========================================");
            log.info("登录信息:");
            log.info("  用户名: admin");
            log.info("  密码: admin123");
            log.info("========================================");
            log.warn("⚠️  请及时登录系统修改默认密码！");
            log.info("========================================");
            
        } catch (Exception e) {
            log.error("❌ 超级管理员初始化失败", e);
        }
    }

    /**
     * 初始化基础角色
     */
    private void initBasicRoles() {
        log.info("🔧 检查基础角色...");
        
        // 创建基础角色
        createRoleIfNotExists("超级管理员", "SUPER_ADMIN");
        createRoleIfNotExists("管理员", "ADMIN");
        createRoleIfNotExists("操作员", "OPERATOR");
        createRoleIfNotExists("审核员", "AUDITOR");
        createRoleIfNotExists("普通用户", "USER");
        
        log.info("✅ 基础角色检查完成");
    }

    /**
     * 创建角色（如果不存在）
     *
     * @param roleName 角色名称
     * @param roleKey  角色标识
     * @return 角色对象
     */
    private SysRole createRoleIfNotExists(String roleName, String roleKey) {
        // 检查是否已存在
        SysRole existingRole = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleKey)
        );
        
        if (existingRole != null) {
            log.debug("角色已存在: {} ({})", roleName, roleKey);
            return existingRole;
        }

        // 创建新角色
        SysRole role = new SysRole();
        role.setId(idGenerator.nextId());
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setStatus(1); // 启用
        role.setDeleted(0); // 未删除
        role.setCreateTime(new Date());
        
        roleMapper.insert(role);
        log.info("✅ 角色创建成功: {} ({}) - roleId={}", roleName, roleKey, role.getId());
        
        return role;
    }
}

