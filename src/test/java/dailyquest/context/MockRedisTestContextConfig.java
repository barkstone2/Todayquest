package dailyquest.context;

import dailyquest.properties.RedisKeyProperties;
import org.mockito.Answers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class MockRedisTestContextConfig {
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    RedisTemplate<String, String> redisTemplate;

    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    RedisKeyProperties redisKeyProperties;
}
