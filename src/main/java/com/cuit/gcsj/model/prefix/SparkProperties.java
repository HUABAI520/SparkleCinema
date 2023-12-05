package com.cuit.gcsj.model.prefix;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author L
 */
@Component
@ConfigurationProperties(prefix = "spark")
@Data
public class SparkProperties implements Serializable {
    private String host;
    private String hdfsUri;
    private String user;
    private int port;
    private String password;
    private String command;
}
