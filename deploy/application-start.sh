CONTAINER_NAME=dailyquest-server
REPOSITORY=barkstone2/dailyquest-server
VERSION=$(cat version.txt)

docker pull $REPOSITORY:$VERSION
./container-start.sh $CONTAINER_NAME $REPOSITORY $VERSION