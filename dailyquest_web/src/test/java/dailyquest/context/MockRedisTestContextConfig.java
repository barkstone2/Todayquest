package dailyquest.context;

import com.ninjasquad.springmockk.MockkBean;
import dailyquest.properties.RedisKeyProperties;
import dailyquest.redis.repository.RedisRepository;
import org.mockito.Answers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class MockRedisTestContextConfig {

    @MockkBean(relaxed = true)
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    RedisRepository redisRepository;

    @MockkBean(relaxed = true)
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    RedisTemplate<String, String> redisTemplate;

    @MockkBean(relaxed = true)
    @MockBean(answer = Answers.RETURNS_SMART_NULLS)
    RedisKeyProperties redisKeyProperties;
}
