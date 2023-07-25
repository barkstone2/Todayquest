CONTAINER_NAME=dailyquest-server
REPOSITORY=barkstone2/dailyquest-server

docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME
docker rmi $(docker -f reference=$REPOSITORY -q)