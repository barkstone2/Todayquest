CONTAINER_NAME=dailyquest-server

if [ "$(docker inspect -f {{.State.Running}} $CONTAINER_NAME)" != "true" ]; then
    echo "Docker container failed to run." >&2
    exit 1
fi