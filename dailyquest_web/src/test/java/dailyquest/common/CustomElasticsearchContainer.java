package dailyquest.common;

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

public class CustomElasticsearchContainer extends ElasticsearchContainer {

    private static final String ELASTIC_SEARCH_DOCKER = "elasticsearch:8.10.2";
    private static final String CLUSTER_NAME = "cluster.name";
    private static final String ELASTIC_SEARCH = "elasticsearch-test";

    private static final class XPACK_SECURITY_HTTP_SSL {
        private static final String BASE = "xpack.security.http.ssl";
        static final String ENABLED = BASE + ".enabled";
        static final String KEYSTORE_PATH = BASE + ".keystore.path";
        static final String KEYSTORE_PASSWORD = BASE + ".keystore.password";
        static final String TRUSTSTORE_PATH = BASE + ".truststore.path";
        static final String TRUSTSTORE_PASSWORD = BASE + ".truststore.password";
        static final String VERIFICATION_MODE = BASE + ".verification_mode";
    }

    private static final String ENABLED_TRUE = "true";
    private static final String KEYSTORE_PATH_DEST = "/usr/share/elasticsearch/config/cert/test-http.p12";
    private static final String VERIFICATION_MODE_CERTIFICATE = "certificate";
    private static final String KEYSTORE_PASSWORD_VALUE = "changeit";
    private static final String USER_PASSWORD = "changeit";

    public CustomElasticsearchContainer() {
        super(DockerImageName.parse(ELASTIC_SEARCH_DOCKER)
                .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch"));
        ImageFromDockerfile image = new ImageFromDockerfile()
                .withDockerfileFromBuilder(builder ->
                        builder.from(ELASTIC_SEARCH_DOCKER)
                                .run("bin/elasticsearch-plugin", "install", "analysis-nori")
                                .build());

        setImage(image);
        this.withCopyToContainer(MountableFile.forClasspathResource("/test-keystore/test-http.p12", MountableFile.DEFAULT_FILE_MODE), KEYSTORE_PATH_DEST);
        this.withPassword(USER_PASSWORD);

        addFixedExposedPort(9205, 9200);
        addEnv(CLUSTER_NAME, ELASTIC_SEARCH);
        addEnv(XPACK_SECURITY_HTTP_SSL.ENABLED, ENABLED_TRUE);
        addEnv(XPACK_SECURITY_HTTP_SSL.KEYSTORE_PATH, KEYSTORE_PATH_DEST);
        addEnv(XPACK_SECURITY_HTTP_SSL.KEYSTORE_PASSWORD, KEYSTORE_PASSWORD_VALUE);
        addEnv(XPACK_SECURITY_HTTP_SSL.TRUSTSTORE_PATH, KEYSTORE_PATH_DEST);
        addEnv(XPACK_SECURITY_HTTP_SSL.TRUSTSTORE_PASSWORD, KEYSTORE_PASSWORD_VALUE);
        addEnv(XPACK_SECURITY_HTTP_SSL.VERIFICATION_MODE, VERIFICATION_MODE_CERTIFICATE);

        setWaitStrategy(
                new LogMessageWaitStrategy().withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)").withStartupTimeout(Duration.ofMinutes(3)));
    }
}