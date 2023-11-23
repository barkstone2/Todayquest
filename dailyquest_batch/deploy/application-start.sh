CONTAINER_NAME=dailyquest-batch
REPOSITORY=barkstone2/dailyquest-batch
VERSION=$(cat /home/ec2-user/app/batch/version.txt)

REMOVE_IMAGE=$(docker images -f "reference=$REPOSITORY" -f "before=$REPOSITORY:$VERSION" -q)

docker stop $CONTAINER_NAME
docker rm $CONTAINER_NAME

if [ -n "$REMOVE_IMAGE" ]; then
    docker rmi $REMOVE_IMAGE
fi

docker pull $REPOSITORY:$VERSION

/home/ec2-user/app/batch/container-start.sh $CONTAINER_NAME $REPOSITORY $VERSION