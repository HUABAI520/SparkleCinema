package com.cuit.gcsj.initializer;

import com.cuit.gcsj.model.prefix.SparkProperties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author L  项目启动时执行连接ssh
 */
@Component
public class SparkInitializer implements CommandLineRunner {


    @Resource
    private SparkProperties sparkProperties;

    @Getter
    private Session session;

    @Override
    public void run(String... args) throws Exception {
        // 在这里执行您的初始化代码
        session = createSession();
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(
                sparkProperties.getUser(),
                sparkProperties.getHost(),
                sparkProperties.getPort()
        );
        session.setPassword(sparkProperties.getPassword());
        session.setUserInfo(createUserInfo());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        return session;
    }

    private UserInfo createUserInfo() {
        return new UserInfo() {
            @Override
            public String getPassphrase() {
                return null;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public boolean promptPassword(String message) {
                return false;
            }

            @Override
            public boolean promptPassphrase(String message) {
                return false;
            }

            @Override
            public boolean promptYesNo(String message) {
                return true;
            }

            @Override
            public void showMessage(String message) {
            }
        };
    }

}
