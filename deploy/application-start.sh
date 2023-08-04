CONTAINER_NAME=dailyquest-server
REPOSITORY=barkstone2/dailyquest-server
VERSION=$(cat /home/ec2-user/app/server/version.txt)

NGINX_PATH=/home/ec2-user/app/nginx
COLOR=blue
NEW_COLOR=green
PORT=8181

IS_BLUE_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$COLOR)

docker pull $REPOSITORY:$VERSION

if [ "$IS_BLUE_UP" != "true" ]; then
    NEW_COLOR=blue
    COLOR=green
    PORT=8080
fi

/home/ec2-user/app/server/container-start.sh $CONTAINER_NAME-$NEW_COLOR $REPOSITORY $VERSION $PORT

sleep 10

IS_NEW_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$NEW_COLOR)

if [ "$IS_NEW_UP" == "true" ]; then
    cp $NGINX_PATH/nginx-$NEW_COLOR.conf $NGINX_PATH/nginx.conf
    docker exec dailyquest-client nginx -s reload

    REMOVE_IMAGE=$(docker images -f "reference=$REPOSITORY" -f "before=$REPOSITORY:$VERSION" -q)
    docker stop $CONTAINER_NAME-$COLOR
    docker rm $CONTAINER_NAME-$COLOR

    if [ -n "$REMOVE_IMAGE" ]; then
        docker rmi $REMOVE_IMAGE
    fi
fi