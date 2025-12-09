package cn.masu.dcs.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置类
 * <p>
 * 解决JavaScript中Long类型精度丢失问题
 * 将所有Long类型序列化为字符串
 * </p>
 *
 * @author zyq
 * @since 2025-12-09
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置ObjectMapper
     * 将Long类型序列化为字符串，避免JavaScript精度丢失
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 创建自定义模块
        SimpleModule simpleModule = new SimpleModule();

        // 将Long类型序列化为字符串
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 注册模块
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}

