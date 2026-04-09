package com.deustchhub.common.util;

import com.deustchhub.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageUtils {

    MessageSource messageSource;

    public MessageUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(ErrorCode errorCode) {
        return messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                LocaleContextHolder.getLocale());
    }

    public String getMessage(ErrorCode errorCode, Object ... args) {
        return messageSource.getMessage(
                errorCode.getMessageKey(),
                args,
                LocaleContextHolder.getLocale());
    }
}
