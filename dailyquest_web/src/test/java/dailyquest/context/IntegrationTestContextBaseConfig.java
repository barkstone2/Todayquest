package dailyquest.context;

import dailyquest.common.MessageUtil;
import dailyquest.config.DataSourceConfig;
import dailyquest.config.JpaAuditingConfiguration;
import dailyquest.config.SecurityConfig;
import dailyquest.config.WebMvcConfig;
import dailyquest.exception.RestApiExceptionHandler;
import dailyquest.jwt.JwtAuthorizationFilter;
import dailyquest.jwt.JwtTokenProvider;
import dailyquest.properties.JwtTokenProperties;
import dailyquest.properties.SecurityOriginProperties;
import dailyquest.properties.SecurityUrlProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        DataSourceConfig.class, SecurityConfig.class, WebMvcConfig.class,
        RestApiExceptionHandler.class, JpaAuditingConfiguration.class, MessageUtil.class,
        JwtAuthorizationFilter.class, JwtTokenProvider.class, JwtTokenProperties.class,
        SecurityOriginProperties.class, SecurityUrlProperties.class,
        UserTestContextConfig.class
})
@EnableAutoConfiguration
@TestConfiguration
public class IntegrationTestContextBaseConfig {
}
