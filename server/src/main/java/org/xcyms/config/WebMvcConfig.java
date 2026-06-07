package org.xcyms.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xcyms.common.enums.BaseEnum;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

/**
 * <p>
 *     web 配置类
 * </p>
 * @author 刘旭
 * @date 2026年01月11日 11:38
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 路由拦截器，校验是否登录
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/druid/**"
                );
    }

    /**
     * 自定义消息转换器
     *
     * @param converters 转换器列表
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 设置时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        // 2. 过滤 null 字段 (满足用户需求)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 3. 反序列化时忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 4. 注册 JavaTimeModule 处理 LocalDateTime (解决报错)
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);

        // 5. 解决 Long 类型前端精度丢失问题
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);

        // 6. 全局枚举处理：实现 BaseEnum 的枚举自动序列化为对象，且支持反序列化
        SimpleModule enumModule = new SimpleModule();

        // 序列化逻辑：BaseEnum -> {"code": 1, "desc": "是"}
        enumModule.addSerializer(BaseEnum.class, new JsonSerializer<>() {
            @Override
            public void serialize(BaseEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeNumberField("code", value.getCode());
                gen.writeStringField("desc", value.getDesc());
                gen.writeEndObject();
            }
        });

        // 反序列化逻辑：支持数字、字符串、对象转枚举
        objectMapper.registerModule(enumModule);
        // 注意：反序列化通常需要更复杂的 Deserializers 注册，
        // 为了简单且不用关心具体类，可以使用 @JsonCreator 的变体或全局配置。
        // 这里推荐在 WebMvcConfig 中配置全局解析：
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        jackson2HttpMessageConverter.setObjectMapper(objectMapper);

        // 将自定义的 converter 放在第一位，确保其生效
        converters.add(0, jackson2HttpMessageConverter);
    }
}
