package dailyquest.context;

import dailyquest.config.ElasticsearchConfig;
import dailyquest.properties.ElasticProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
        ElasticsearchConfig.class,
        ElasticProperties.class,
})
@TestConfiguration
public class ElasticsearchTestContextConfig {
}
