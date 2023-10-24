package dailyquest.config;

import dailyquest.properties.ElasticProperties;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@RequiredArgsConstructor
@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticProperties elasticProperties;

    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {

        return ClientConfiguration.builder()
                .connectedTo(elasticProperties.combineHostAndPort())
                .usingSsl()
                .withBasicAuth(elasticProperties.getUsername(), elasticProperties.getPassword())
                .build();
    }

}
