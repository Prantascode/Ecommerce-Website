package com.pranta.ecommerce.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "sslcommerz")
@Data
public class SSLCommerzConfig {
    private String storeId;
    private String storePassword;
    private String baseUrl;
    private String initUrl;
    private String validationUrl;
}
