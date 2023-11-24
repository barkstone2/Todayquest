package dailyquest.common;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomRedisContainer extends RedisContainer {

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public CustomRedisContainer() {
        super(DockerImageName.parse("redis:latest"));
        this.withCopyFileToContainer(MountableFile.forClasspathResource("/script/redis_init.sh", 777), "/data/redis_init.sh");
        addFixedExposedPort(6378, 6379);
    }

    public void initRedis() throws IOException, InterruptedException {
        if(initialized.get()) return;
        initialized.set(true);
        this.execInContainer("/data/redis_init.sh");
    }
}
