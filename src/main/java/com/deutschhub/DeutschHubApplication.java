package com.deutschhub;

import com.deutschhub.common.exception.ErrorCode;
import com.deutschhub.common.util.MessageUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Locale;

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
        System.out.println(messageUtils.getMessage(ErrorCode.UNCATEGORIZED_EXCEPTION, null, Locale.forLanguageTag("vi")));
    }
}