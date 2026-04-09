package com.deustchhub;

import com.deustchhub.common.exception.ErrorCode;
import com.deustchhub.common.util.MessageUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        }
)
public class DeutschHubApplication {
    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(DeutschHubApplication.class, args);

        MessageUtils messageUtils = context.getBean(MessageUtils.class);

        ErrorCode error = ErrorCode.UNCATEGORIZED_EXCEPTION;

        String message = messageUtils.getMessage(error);
        int code = error.getErrorCode();

        System.out.println(code + " - " + message);
    }
}