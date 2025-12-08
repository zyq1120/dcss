package cn.masu.dcs.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置
 * <p>
 * 配置内容：
 * 1. 分页插件 - 支持MySQL分页查询
 * 2. 乐观锁插件 - 支持并发更新控制
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus拦截器配置
     * <p>
     * 包含以下插件：
     * 1. 分页插件（PaginationInnerInterceptor）
     * 2. 乐观锁插件（OptimisticLockerInnerInterceptor）
     * </p>
     *
     * @return MyBatis-Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 添加分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页最大数据量（防止恶意查询）
        paginationInterceptor.setMaxLimit(1000L);
        // 设置分页溢出处理（超过最大页时不处理，返回空）
        paginationInterceptor.setOverflow(false);

        interceptor.addInnerInterceptor(paginationInterceptor);

        // 2. 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }
}

