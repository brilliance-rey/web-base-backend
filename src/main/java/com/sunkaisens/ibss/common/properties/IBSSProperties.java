package com.sunkaisens.ibss.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ibss")
public class IBSSProperties {

    private ShiroProperties shiro = new ShiroProperties();

    private boolean openAopLog = true;

}
