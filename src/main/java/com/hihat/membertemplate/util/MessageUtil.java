package com.hihat.membertemplate.util;

import org.springframework.context.MessageSource;

import java.util.Locale;

public class MessageUtil {

    public static String getMessage(MessageSource messageSource, String messageName) {
        return messageSource.getMessage(messageName, null, Locale.getDefault());
    }

}
