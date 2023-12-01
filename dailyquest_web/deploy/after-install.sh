REDIS_CONTAINER_NAME=dailyquest-redis
IS_REDIS_RUNNING=$(docker inspect -f {{.State.Running}} $REDIS_CONTAINER_NAME)

if [ "$IS_REDIS_RUNNING" != "true" ]; then
    docker start $REDIS_CONTAINER_NAME
fi
