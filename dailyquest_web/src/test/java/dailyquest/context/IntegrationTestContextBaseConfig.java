package dailyquest.context;

import dailyquest.common.MessageUtil;
import dailyquest.config.DataSourceConfig;
import dailyquest.config.JpaAuditingConfiguration;
import dailyquest.config.SecurityConfig;
import dailyquest.config.WebMvcConfig;
import dailyquest.exception.RestApiExceptionHandler;
import dailyquest.jwt.JwtAuthorizationFilter;
import dailyquest.jwt.JwtTokenProvider;
import dailyquest.properties.SecurityOriginProperties;
import dailyquest.properties.SecurityUrlProperties;
import dailyquest.user.entity.UserInfo;
import dailyquest.user.repository.UserRepository;
import dailyquest.user.service.UserService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import({
        DataSourceConfig.class, SecurityConfig.class, WebMvcConfig.class,
        RestApiExceptionHandler.class, JpaAuditingConfiguration.class, MessageUtil.class,
        JwtAuthorizationFilter.class, JwtTokenProvider.class,
        SecurityOriginProperties.class, SecurityUrlProperties.class,
        UserService.class
})
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@EntityScan(basePackageClasses = UserInfo.class)
@EnableAutoConfiguration
@TestConfiguration
public class IntegrationTestContextBaseConfig {
}
