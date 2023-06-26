CONTAINER_NAME=todayquest-server
REPOSITORY=barkstone2/todayquest-server
VERSION=$(cat version.txt)

docker pull $REPOSITORY:$VERSION
./container-start.sh $CONTAINER_NAME $REPOSITORY $VERSION