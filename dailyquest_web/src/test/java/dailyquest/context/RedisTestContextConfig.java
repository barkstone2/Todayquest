package dailyquest.context;

import dailyquest.config.RedisConfig;
import dailyquest.properties.RedisKeyProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        RedisConfig.class,
        RedisKeyProperties.class,
})
@TestConfiguration
public class RedisTestContextConfig {
}
