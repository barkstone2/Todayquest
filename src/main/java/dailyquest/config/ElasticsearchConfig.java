package dailyquest.config;

import dailyquest.properties.ElasticProperties;
import lombok.RequiredArgsConstructor;
import org.apache.http.ssl.SSLContextBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@EnableElasticsearchRepositories
@RequiredArgsConstructor
@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    private final ElasticProperties elasticProperties;

    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        SSLContext sslContext;
        try {
            sslContext = SSLContextBuilder.create().loadTrustMaterial(new File(elasticProperties.getTruststore().getLocation()), elasticProperties.getTruststore().getPassword().toCharArray()).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException |
                 IOException e) {
            throw new RuntimeException(e);
        }

        return ClientConfiguration.builder()
                .connectedTo(elasticProperties.combineHostAndPort())
                .usingSsl(sslContext)
                .withBasicAuth(elasticProperties.getUsername(), elasticProperties.getPassword())
                .build();
    }

}
