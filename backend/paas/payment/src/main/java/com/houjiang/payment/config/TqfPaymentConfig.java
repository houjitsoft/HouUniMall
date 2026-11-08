package com.houjiang.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 通企付支付配置
 * 环境参数、密钥、签名配置
 * 
 * @author Amos
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment.tqf")
public class TqfPaymentConfig {
    
    /**
     * 生产环境URL
     */
    private String apiUrl = "https://tp.allinpay.com/pay/api/";
    
    /**
     * 商户号
     */
    private String merchantNo = "E249974037860";
    
    /**
     * AppId
     */
    private String appId = "66384647e4b0148f4bb07829";
    
    /**
     * MD5密钥
     */
    private String md5Key = "r7yu2j8f1bjtv9d57c47y8bpobwsfj6g8ywktyvkilj40uxjgxarrocvbue2lg4d6wciropth06c8k35pywtn085v7umo0f0qnk63m92c0z0xheqlncy73z1ofrxchwi";
    
    /**
     * 商户RSA私钥
     */
    private String rsaPrivateKey = "MIIEwAIBADANBgkqhkiG9w0BAQEFAASCBKowggSmAgEAAoIBAQDNCH60TTVuLrDaj0FezKvIN23jYIiWH0Fp6RGmdGSVF5i+ASA+hxVb35g887QXVrCy2/61EoDBwgm3XwGO9yxXBIhMH8yahBvcgnJ+kIwPV+3MTvKReTPXH3l5nSVkH4UMtysgsvpC4cNQXQCAihV1X9oYk4uWNrtIw22l7OsurGRqwGq6qogn68hmgoVmox5ZajLWacZegCKyRbCGw+r9bHkrPhmewgUT0iWntB5n7ssTCrpLNbufkr4CLBl3lQCRTGxZgBwu5D1QNQ7EgivpOWS6YU6uwvNn8edSWqt2npUv9F9a/jBno3Ztjg4McRxMShPsr9ZmyLa2Dn3acTu3AgMBAAECggEBAL1gzE9TRTe5LqchTJr4Zu6uq/Qp9N7cjbn6ic/6j/DZ874EIjk+4i6S7vXVj5FDBECgTLoJFh5hUEIsIa9ghHb3e8D8WiqaPXXGk2RttMBzUfS1Mv3FOmnH65a13JSHPIhLIy23wspF5vZygIU5hap9V1/94B9l/ESwJbXtiCj5cSiDK2riqlwhm8Mmy/taFharaKdhI7/ajRA3t7LmKv55T1jnLP46mymeCnnvQR8hAWNzKksHwVK6s6sbihPlbsJt+PODMElZwloOkOqX7+FNew1JCdljVQjuj8FOr3nvuCr4rc6ZGfZv64D7Vpku69e4jBYdYgzIwpheLE+Pl2kCgYEA5R4qQYi94LjFfSV2JdaYnIsjtmnqs1vYgiGmpMDTyOC5ltKZEVsLpArnRRGiyZ6yIFMnxnj2IswWNkp3uVuU5G8dzPSHJhoeRMBo18PbnHm6eEPWkCCUVmXLv83sk6IamPQfktJ6CsJyhauW2i3ZuiYQWz0K91Y3kl6P1ST8fDsCgYEA5Rbq/QwXL1718oY/J955Pn8YlypQ6+u8bR+HFH2xYeFFK1+ZbIFOg9S2b6KzfugB4nL3IWso47NDzKGttryDgVUK2e+PONKO4kvZYVF3D7C8j7C4ppDvOLkBF4TpCcHeX5StQgD2F+++AaqA1g60Hrja6Pvc8tBbfFyIWFK/0rUCgYEA2YO7xh+VdCS2iMrp0Z5boDKQIvuG7+RANbrQxBS0ez6tsrwYyVtQP9qCGRZXH2Zj4UBQwKHRutNaOwyTgQuq3PuIpS14qPmextKMNlsgwPrnxolQx9/GNAfMWTmcYcRBz/fjibX8Wv6jTrfKLTPeQlUkrhnTsWDOvUy87DOC6EUCgYEAii4aq2thiLc0joafDYNJioLK6FMj4EmerAt6RMfT+IASYqkVN7d/DlF9gTSYJBH5IQfLPKMQVNfKK2HSEAkBIT+UfgPbVaDbgm/RvLuQnywxcFJd7ko5oPMmT3NhxPrlEK3zG37M8X8wEn0vnO3dgzJpCGLy0JsQs6B0tSGkhokCgYEA0aJHS85KhPf/QOyAuxxZ2EyJHYQw8bDZt24htuvXre2McDL8CATcv72rxrRub7OeL5fvsnIVKP9UVj4N2ahzTh4gyQWivK14hgbq0DhIgmlPnX2PG1YoWqo7O4Qzi2mUv0AoehntmeZGIT4sBH06RWBHvx/6lvcH3bjY54lemUk=";
    
    /**
     * 通企付RSA公钥
     */
    private String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhL54Juhy3A2czrhMiw4d156Pn5/1XjvnW09P/s3stIeO4NT6BLaqQh28t8TvwiT7/BBj27d1AcV6r2y0f8/cYgwUDncegd9GneN0g9Im2mI/Ybo1vAUnYk7dkS9keL+1FVi2s6tjhVqSL9jpthnvtdO5sA1xRlXY+psBtv9Zh3x4Te8hwsJIPpMV328pdUpSwGrD5pu6IxqXrLjhV5/PkmVsg+v383BK0dg6nIhYbJRe7b8587migk04g6Qek1c8U85A0tOcLTM4IxHZ6GzWMrPeQUjI8KMlvpISOUQThJM2MTd+4KMmbty0vjzPAwYopVDmzdpOvT6qlVBFwtfDJQIDAQAB";
    
    /**
     * 商户SM2私钥（国密）
     */
    private String sm2PrivateKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgao7pySLVxhCP21TE9HbQthF8jS73qoLeIzOho/QgFBWgCgYIKoEcz1UBgi2hRANCAASjijfm7cyqzaxLZARpzqD7wEsqb4nI6+/8MzrOvp9/J10wJ2UeuO8uIiYtsrF78hnJuPAWYqoXRJOLDvTCqP7X";
    
    /**
     * 通企付SM2公钥（国密）
     */
    private String sm2PublicKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEORXfgg6f5M5BAo8gn7CpT5fcim445e//S1jBfps2xU16DelWR2N4JwaawkXv3BZl+TK8VUa+ZlJit9Rj4EuHuw==";
    
    /**
     * 默认签名方式 MD5/RSA/SM2
     */
    private String signType = "MD5";
    
    /**
     * 连接超时时间(毫秒)
     */
    private Integer connectTimeout = 30000;
    
    /**
     * 读取超时时间(毫秒)
     */
    private Integer readTimeout = 30000;
}
