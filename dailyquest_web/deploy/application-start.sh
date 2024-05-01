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

docker tag $REPOSITORY:$VERSION $CONTAINER_NAME:$NEW_COLOR
docker rmi $REPOSITORY:$VERSION

/home/ec2-user/app/server/container-start.sh $CONTAINER_NAME-$NEW_COLOR $CONTAINER_NAME $NEW_COLOR $PORT

sleep 10

IS_NEW_UP=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$NEW_COLOR)
if [ "$IS_NEW_UP" == "true" ]; then
    docker logs $CONTAINER_NAME-$NEW_COLOR | grep 'Completed initialization'
    NEW_CONTAINER_INIT=$?
    while [ NEW_CONTAINER_INIT == 0 ]
    do
        sleep 1
        docker logs $CONTAINER_NAME-$NEW_COLOR | grep 'Completed initialization'
        NEW_CONTAINER_INIT=$?
    done

    cp $NGINX_PATH/nginx-$NEW_COLOR.conf $NGINX_PATH/nginx.conf
    docker exec dailyquest-client nginx -s reload

    REMOVE_IMAGE=$(docker images -f "reference=$CONTAINER_NAME:$COLOR" -q)

    # 컨테이너가 존재하지 않아도 스크립트가 중단되지 않음
    docker stop $CONTAINER_NAME-$COLOR
    docker rm $CONTAINER_NAME-$COLOR

    # 이미지가 존재하지 않으면 스크립트 중단됨
    # 기존 이미지가 존재할 때만 제거
    if [ -n "$REMOVE_IMAGE" ]; then
        docker rmi $REMOVE_IMAGE
    fi
fi
