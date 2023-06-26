CONTAINER_NAME=todayquest-server
REPOSITORY=barkstone2/todayquest-server

docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME
docker rmi $(docker -f reference=$REPOSITORY -q)