CONTAINER_NAME=dailyquest-batch

IS_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME)

if [ "$IS_UP" != "true" ]; then
    echo "Docker container failed to run." >&2
    exit 1
fi
