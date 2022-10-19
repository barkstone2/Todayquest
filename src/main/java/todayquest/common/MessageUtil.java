package todayquest.common;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageUtil {
    private static MessageSource messageSource;

    public MessageUtil(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public static String getMessage(String code) {
        return messageSource.getMessage(code, null, Locale.ROOT);
    }

    public static String getMessage(String code, Object ...args) {
        return messageSource.getMessage(code, args, Locale.ROOT);
    }

    public static String getMessageWithLocale(String code, Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }

    public static String getMessageWithLocale(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

}

