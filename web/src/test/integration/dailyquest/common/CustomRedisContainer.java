package dailyquest.common;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

public class CustomRedisContainer extends RedisContainer {

    public CustomRedisContainer() {
        super(DockerImageName.parse("redis:latest"));
        this.withCopyFileToContainer(MountableFile.forClasspathResource("/script/redis_init.sh", 777), "/data/redis_init.sh");
        addFixedExposedPort(6378, 6379);
    }

    public void initRedis() throws IOException, InterruptedException {
        this.execInContainer("/data/redis_init.sh");
    }
}
