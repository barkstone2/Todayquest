package dailyquest.common

import com.github.dockerjava.api.command.CreateContainerCmd
import org.opensearch.testcontainers.OpensearchContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import org.testcontainers.utility.DockerImageName

class CustomOpenSearchContainer : OpensearchContainer<CustomOpenSearchContainer>(DockerImageName.parse(OPEN_SEARCH_DOCKER_IMAGE)) {
    init {
        val image = ImageFromDockerfile()
            .withDockerfileFromBuilder { builder: DockerfileBuilder ->
                builder.from(OPEN_SEARCH_DOCKER_IMAGE)
                    .run("bin/opensearch-plugin", "install", "analysis-nori")
                    .build()
            }
        setImage(image)
        this.withSecurityEnabled()
        this.withCreateContainerCmdModifier { cmd: CreateContainerCmd ->
            cmd.hostConfig!!
                .withMemory(1024 * 1024 * 1024L)
        }

        addFixedExposedPort(9205, 9200)
    }

    companion object {
        private const val OPEN_SEARCH_DOCKER_IMAGE = "opensearchproject/opensearch:2.5.0"
    }
}