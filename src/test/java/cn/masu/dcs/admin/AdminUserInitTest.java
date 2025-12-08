package cn.masu.dcs.admin;

import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.document_classification_system_springboot.DocumentClassificationSystemSpringbootApplication;
import cn.masu.dcs.entity.SysRole;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.entity.SysUserRole;
import cn.masu.dcs.mapper.SysRoleMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import cn.masu.dcs.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

/**
 * 超级管理员初始化测试类
 * <p>
 * 用于初始化系统的超级管理员账户和基础角色
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@SpringBootTest(classes = DocumentClassificationSystemSpringbootApplication.class)
public class AdminUserInitTest {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    /**
     * 初始化超级管理员
     * <p>
     * 用户名: admin
     * 密码: admin123
     * 角色: 超级管理员
     * </p>
     */
    @Test
    public void initSuperAdmin() {
        log.info("========================================");
        log.info("开始初始化超级管理员账户");
        log.info("========================================");

        // 1. 检查超级管理员角色是否存在
        SysRole adminRole = checkAndCreateAdminRole();
        log.info("✅ 超级管理员角色准备完成: roleId={}, roleName={}", adminRole.getId(), adminRole.getRoleName());

        // 2. 检查admin用户是否已存在
        SysUser existingUser = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );

        if (existingUser != null) {
            log.warn("⚠️  用户 'admin' 已存在，跳过创建");
            log.info("用户信息: userId={}, username={}, nickname={}",
                    existingUser.getId(), existingUser.getUsername(), existingUser.getNickname());
            return;
        }

        // 3. 创建超级管理员用户
        SysUser adminUser = new SysUser();
        adminUser.setId(idGenerator.nextId());
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123")); // 密码加密
        adminUser.setNickname("超级管理员");
        adminUser.setEmail("admin@system.com");
        adminUser.setPhone("13800138000");
        adminUser.setStatus(1); // 启用
        adminUser.setDeleted(0); // 未删除
        adminUser.setTokenVersion(0);
        adminUser.setVersion(0);
        adminUser.setCreateTime(new Date());
        adminUser.setUpdateTime(new Date());

        int userInsertResult = userMapper.insert(adminUser);
        if (userInsertResult > 0) {
            log.info("✅ 超级管理员用户创建成功");
            log.info("   用户ID: {}", adminUser.getId());
            log.info("   用户名: {}", adminUser.getUsername());
            log.info("   昵称: {}", adminUser.getNickname());
            log.info("   密码: admin123 (请及时修改)");
        } else {
            log.error("❌ 超级管理员用户创建失败");
            return;
        }

        // 4. 分配超级管理员角色
        SysUserRole userRole = new SysUserRole();
        userRole.setId(idGenerator.nextId());
        userRole.setUserId(adminUser.getId());
        userRole.setRoleId(adminRole.getId());

        int roleAssignResult = userRoleMapper.insert(userRole);
        if (roleAssignResult > 0) {
            log.info("✅ 角色分配成功: userId={}, roleId={}", adminUser.getId(), adminRole.getId());
        } else {
            log.error("❌ 角色分配失败");
            return;
        }

        // 5. 完成提示
        log.info("========================================");
        log.info("✅ 超级管理员初始化完成！");
        log.info("========================================");
        log.info("登录信息:");
        log.info("  用户名: admin");
        log.info("  密码: admin123");
        log.info("  角色: 超级管理员");
        log.info("========================================");
        log.info("⚠️  请及时登录系统修改默认密码！");
        log.info("========================================");
    }

    /**
     * 检查并创建超级管理员角色
     *
     * @return 超级管理员角色对象
     */
    private SysRole checkAndCreateAdminRole() {
        // 查询是否已存在超级管理员角色（按 role_key 查询）
        SysRole existingRole = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, "SUPER_ADMIN")
                .or()
                .eq(SysRole::getRoleName, "超级管理员")
        );

        if (existingRole != null) {
            log.info("超级管理员角色已存在: roleId={}, roleName={}, roleKey={}",
                    existingRole.getId(), existingRole.getRoleName(), existingRole.getRoleKey());
            return existingRole;
        }

        // 创建超级管理员角色
        SysRole adminRole = new SysRole();
        adminRole.setId(idGenerator.nextId());
        adminRole.setRoleName("超级管理员");
        adminRole.setRoleKey("SUPER_ADMIN");
        adminRole.setStatus(1); // 启用
        adminRole.setDeleted(0); // 未删除
        adminRole.setCreateTime(new Date());

        int result = roleMapper.insert(adminRole);
        if (result > 0) {
            log.info("超级管理员角色创建成功: roleId={}", adminRole.getId());
        } else {
            throw new RuntimeException("超级管理员角色创建失败");
        }

        return adminRole;
    }

    /**
     * 创建普通管理员角色（可选）
     */
    @Test
    public void initAdminRole() {
        log.info("创建普通管理员角色");

        SysRole existingRole = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, "ADMIN")
        );

        if (existingRole != null) {
            log.info("普通管理员角色已存在");
            return;
        }

        SysRole adminRole = new SysRole();
        adminRole.setId(idGenerator.nextId());
        adminRole.setRoleName("管理员");
        adminRole.setRoleKey("ADMIN");
        adminRole.setStatus(1);
        adminRole.setDeleted(0);
        adminRole.setCreateTime(new Date());

        roleMapper.insert(adminRole);
        log.info("✅ 普通管理员角色创建成功: roleId={}", adminRole.getId());
    }

    /**
     * 创建操作员角色（可选）
     */
    @Test
    public void initOperatorRole() {
        log.info("创建操作员角色");

        SysRole existingRole = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, "OPERATOR")
        );

        if (existingRole != null) {
            log.info("操作员角色已存在");
            return;
        }

        SysRole operatorRole = new SysRole();
        operatorRole.setId(idGenerator.nextId());
        operatorRole.setRoleName("操作员");
        operatorRole.setRoleKey("OPERATOR");
        operatorRole.setStatus(1);
        operatorRole.setDeleted(0);
        operatorRole.setCreateTime(new Date());

        roleMapper.insert(operatorRole);
        log.info("✅ 操作员角色创建成功: roleId={}", operatorRole.getId());
    }

    /**
     * 初始化所有基础角色
     */
    @Test
    public void initAllRoles() {
        log.info("========================================");
        log.info("初始化所有基础角色");
        log.info("========================================");

        // 创建超级管理员角色
        checkAndCreateAdminRole();

        // 创建普通管理员角色
        createRoleIfNotExists("管理员", "ADMIN");

        // 创建操作员角色
        createRoleIfNotExists("操作员", "OPERATOR");

        // 创建审核员角色
        createRoleIfNotExists("审核员", "AUDITOR");

        // 创建普通用户角色
        createRoleIfNotExists("普通用户", "USER");

        log.info("========================================");
        log.info("✅ 所有基础角色初始化完成");
        log.info("========================================");
    }

    /**
     * 创建角色（如果不存在）
     *
     * @param roleName 角色名称
     * @param roleKey  角色标识
     */
    private void createRoleIfNotExists(String roleName, String roleKey) {
        SysRole existingRole = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleKey)
        );

        if (existingRole != null) {
            log.info("角色已存在: {} ({})", roleName, roleKey);
            return;
        }

        SysRole role = new SysRole();
        role.setId(idGenerator.nextId());
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setStatus(1);
        role.setDeleted(0);
        role.setCreateTime(new Date());

        roleMapper.insert(role);
        log.info("✅ 角色创建成功: {} ({}) - roleId={}", roleName, roleKey, role.getId());
    }

    /**
     * 查询超级管理员信息
     */
    @Test
    public void queryAdminInfo() {
        log.info("========================================");
        log.info("查询超级管理员信息");
        log.info("========================================");

        SysUser adminUser = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin")
        );

        if (adminUser == null) {
            log.warn("⚠️  超级管理员账户不存在，请先运行 initSuperAdmin() 创建");
            return;
        }

        log.info("用户ID: {}", adminUser.getId());
        log.info("用户名: {}", adminUser.getUsername());
        log.info("昵称: {}", adminUser.getNickname());
        log.info("邮箱: {}", adminUser.getEmail());
        log.info("手机: {}", adminUser.getPhone());
        log.info("状态: {}", adminUser.getStatus() == 1 ? "启用" : "禁用");
        log.info("创建时间: {}", adminUser.getCreateTime());

        // 查询角色信息
        SysUserRole userRole = userRoleMapper.selectOne(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, adminUser.getId())
        );

        if (userRole != null) {
            SysRole role = roleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                log.info("角色: {} ({})", role.getRoleName(), role.getRoleKey());
            }
        }

        log.info("========================================");
    }
}

