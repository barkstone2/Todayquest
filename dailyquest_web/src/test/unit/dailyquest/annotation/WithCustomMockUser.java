package dailyquest.annotation;

import org.springframework.security.test.context.support.WithSecurityContext;
import dailyquest.common.WithMockCustomUserSecurityContextFactory;
import dailyquest.user.entity.ProviderType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithCustomMockUser {
    long userId() default 1;
    String nickname() default "nickname";
    ProviderType providerType() default ProviderType.GOOGLE;
}
