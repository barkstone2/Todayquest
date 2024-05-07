CONTAINER_NAME=dailyquest-server

COLOR=blue
OTHER_COLOR=green

IS_BLUE_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$COLOR)
IS_GREEN_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$OTHER_COLOR)

if [ "$IS_BLUE_UP" != "true" ] && [ "$IS_GREEN_UP" != "true" ]; then
    echo "Docker container failed to run." >&2
    exit 1
fi
