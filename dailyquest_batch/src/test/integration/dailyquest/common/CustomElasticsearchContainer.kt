package dailyquest.common

import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.time.Duration

class CustomElasticsearchContainer : ElasticsearchContainer(
    DockerImageName.parse(ELASTIC_SEARCH_DOCKER)
        .asCompatibleSubstituteFor("docker.elastic.co/elasticsearch/elasticsearch")
) {
    private object XPACK_SECURITY_HTTP_SSL {
        private const val BASE = "xpack.security.http.ssl"
        const val ENABLED = "$BASE.enabled"
        const val KEYSTORE_PATH = "$BASE.keystore.path"
        const val KEYSTORE_PASSWORD = "$BASE.keystore.password"
        const val TRUSTSTORE_PATH = "$BASE.truststore.path"
        const val TRUSTSTORE_PASSWORD = "$BASE.truststore.password"
        const val VERIFICATION_MODE = "$BASE.verification_mode"
    }

    init {
        val image = ImageFromDockerfile()
            .withDockerfileFromBuilder { builder: DockerfileBuilder ->
                builder.from(ELASTIC_SEARCH_DOCKER)
                    .run("bin/elasticsearch-plugin", "install", "analysis-nori")
                    .build()
            }
        setImage(image)

        withCopyToContainer(MountableFile.forClasspathResource(
            "/test-keystore/test-http.p12",
            MountableFile.DEFAULT_FILE_MODE
        ), KEYSTORE_PATH_DEST)

        withPassword(USER_PASSWORD)
        addFixedExposedPort(9205, 9200)
        addEnv(CLUSTER_NAME, ELASTIC_SEARCH)
        addEnv(XPACK_SECURITY_HTTP_SSL.ENABLED, ENABLED_TRUE)
        addEnv(XPACK_SECURITY_HTTP_SSL.KEYSTORE_PATH, KEYSTORE_PATH_DEST)
        addEnv(XPACK_SECURITY_HTTP_SSL.KEYSTORE_PASSWORD, KEYSTORE_PASSWORD_VALUE)
        addEnv(XPACK_SECURITY_HTTP_SSL.TRUSTSTORE_PATH, KEYSTORE_PATH_DEST)
        addEnv(XPACK_SECURITY_HTTP_SSL.TRUSTSTORE_PASSWORD, KEYSTORE_PASSWORD_VALUE)
        addEnv(XPACK_SECURITY_HTTP_SSL.VERIFICATION_MODE, VERIFICATION_MODE_CERTIFICATE)
        setWaitStrategy(
            LogMessageWaitStrategy().withRegEx(".*(\"message\":\\s?\"started[\\s?|\"].*|] started\n$)")
                .withStartupTimeout(
                    Duration.ofMinutes(3)
                )
        )
    }

    companion object {
        private const val ELASTIC_SEARCH_DOCKER = "elasticsearch:8.10.2"
        private const val CLUSTER_NAME = "cluster.name"
        private const val ELASTIC_SEARCH = "elasticsearch-test"
        private const val ENABLED_TRUE = "true"
        private const val KEYSTORE_PATH_DEST = "/usr/share/elasticsearch/config/cert/test-http.p12"
        private const val VERIFICATION_MODE_CERTIFICATE = "certificate"
        private const val KEYSTORE_PASSWORD_VALUE = "changeit"
        private const val USER_PASSWORD = "changeit"
    }
}