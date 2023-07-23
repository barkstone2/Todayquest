CONTAINER_NAME=todayquest-server
REPOSITORY=barkstone2/todayquest-server
REMOVE_IMAGE=$(docker images -f reference=$REPOSITORY -q)

docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME
if [ -n "$REMOVE_IMAGE" ]; then
    docker rmi $REMOVE_IMAGE
fi