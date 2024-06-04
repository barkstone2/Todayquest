package dailyquest.config;

import dailyquest.properties.ElasticProperties;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class ElasticsearchConfig extends AbstractOpenSearchConfiguration {

    private final ElasticProperties elasticProperties;

    @Bean
    @Override
    public RestHighLevelClient opensearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(elasticProperties.getElasticAddress())
                .usingSsl()
                .withConnectTimeout(elasticProperties.getConnectionTimeoutMillis())
                .withSocketTimeout(elasticProperties.getSocketTimeoutMillis())
                .withBasicAuth(elasticProperties.getUsername(), elasticProperties.getPassword())
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
