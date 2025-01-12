package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
*用来从外部配置文件中加载阿里云 OSS 的配置信息（如 endpoint、accessKeyId、accessKeySecret 和 bucketName），
* 并将这些信息注入到一个 Java 类中。通过 @ConfigurationProperties 注解，
* Spring Boot 能够自动读取配置并将它们绑定到类的字段上，简化了配置的读取和管理。
 */
@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;    //阿里云 OSS 的服务端点，通常由阿里云提供，表示 OSS 服务所在的区域和地址
    private String accessKeyId;  //阿里云账号的 AccessKey ID，用于身份认证
    private String accessKeySecret;  //阿里云账号的 AccessKey Secret，用于身份认证
    private String bucketName;      //OSS 存储桶的名称，存储桶是用于存储对象的逻辑分组

}
