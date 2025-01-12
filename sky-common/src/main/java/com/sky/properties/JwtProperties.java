package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;  //管理员端生成 JWT 令牌时使用的密钥
    private long adminTtl;  //管理员端 JWT 令牌的有效时间（TTL，Time-To-Live），通常以秒为单位
    private String adminTokenName; //管理员端 JWT 令牌在请求中的名称，通常是 HTTP 请求头中的 Authorization 或自定义的字段

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;

}
