package org.example.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.example.springboot.controller.MenuController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class LocalDateTimeConfig implements InitializingBean {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalDateTimeConfig.class);

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("LocalDateTimeConfig 初始化完成，使用的日期时间格式: {}", DATE_TIME_PATTERN);
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        LOGGER.info("配置 Jackson2ObjectMapperBuilderCustomizer 使用日期格式: {}", DATE_TIME_PATTERN);
        return builder -> {
            // 添加JavaTimeModule以支持Java 8日期时间类型
            builder.modules(new JavaTimeModule());
            
            // 配置序列化器
            builder.serializers(new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            
            // 配置反序列化器
            builder.deserializers(new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
            
            // 禁用时间戳写入
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            
            LOGGER.info("Jackson builder 已配置完成");
        };
    }

    /**
     * 注册一个自定义的字符串到LocalDateTime的转换器
     * 支持多种日期时间格式：
     * 1. 自定义格式：yyyy-MM-dd HH:mm:ss
     * 2. ISO格式带Z后缀：2025-05-28T01:00:00.000Z
     * 3. ISO格式带时区：2025-05-28T01:00:00+08:00
     * 4. 标准ISO格式：2025-05-28T01:00:00
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        LOGGER.info("注册 LocalDateTime 转换器");
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                LOGGER.info("尝试转换日期时间: {}", source);
                
                if (source == null || source.trim().isEmpty()) {
                    LOGGER.warn("空日期时间字符串");
                    return null;
                }

                // 按照优先级依次尝试不同的解析方式
                try {
                    // 1. 尝试使用自定义格式
                    return LocalDateTime.parse(source, DATE_TIME_FORMATTER);
                } catch (DateTimeParseException e1) {
                    LOGGER.debug("标准格式转换失败，尝试其他格式: {}", e1.getMessage());
                    
                    try {
                        // 2. 尝试解析带Z后缀的ISO格式（UTC时间）
                        if (source.endsWith("Z")) {
                            ZonedDateTime zdt = ZonedDateTime.parse(source);
                            return zdt.toLocalDateTime();
                        }
                        
                        // 3. 尝试解析带时区的ISO格式
                        if (source.contains("+") || source.contains("-")) {
                            ZonedDateTime zdt = ZonedDateTime.parse(source);
                            return zdt.toLocalDateTime();
                        }
                        
                        // 4. 尝试标准ISO格式（不含时区）
                        return LocalDateTime.parse(source);
                    } catch (Exception e2) {
                        LOGGER.error("所有格式转换失败: {}", e2.getMessage());
                        throw new IllegalArgumentException("无法解析日期时间: " + source, e2);
                    }
                }
            }
        };
    }
    
    /**
     * 直接配置 MappingJackson2HttpMessageConverter
     */
    @Bean
    @Primary
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        LOGGER.info("配置 MappingJackson2HttpMessageConverter");
        
        // 创建JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // 添加序列化器和反序列化器
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        
        // 创建ObjectMapper
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(javaTimeModule)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        
        LOGGER.info("已创建配置好的 ObjectMapper，支持多种日期时间格式");
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}