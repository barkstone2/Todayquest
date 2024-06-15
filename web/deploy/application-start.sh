CONTAINER_NAME=dailyquest-server
REPOSITORY=barkstone2/dailyquest-server
VERSION=$(cat /home/ec2-user/app/server/version.txt)

NGINX_PATH=/home/ec2-user/app/nginx
COLOR=blue
NEW_COLOR=green
PORT=8181

resolve_new_color() {
    local is_blue_up=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$COLOR)
    if [ "$is_blue_up" != "true" ]; then
        NEW_COLOR=blue
        COLOR=green
        PORT=8080
    fi
}

replace_docker_image() {
    docker pull $REPOSITORY:$VERSION
    docker tag $REPOSITORY:$VERSION $CONTAINER_NAME:$NEW_COLOR
    docker rmi $REPOSITORY:$VERSION
}

start_new_container() {
    /home/ec2-user/app/server/container-start.sh $CONTAINER_NAME-$NEW_COLOR $CONTAINER_NAME $NEW_COLOR $PORT
}

remove_new_container_and_image() {
    docker stop $CONTAINER_NAME-$NEW_COLOR
    docker rm $CONTAINER_NAME-$NEW_COLOR
    docker rmi $CONTAINER_NAME:$NEW_COLOR
}

validate_new_container() {
    sleep 30
    local timeout=180
    local interval=5
    local elapsed=0
    local is_new_up=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$NEW_COLOR)
    if [ "$is_new_up" == "true" ]; then
        while [ $elapsed -lt $timeout ]; do
            docker logs $CONTAINER_NAME-$NEW_COLOR | grep 'Started DailyquestApplication'
            is_new_init=$?
            if [ $is_new_init -eq 0 ]; then
                sleep 30
                break
            fi
            sleep $interval
            elapsed=$((elapsed + interval))
        done
    fi

    is_new_up=$(docker inspect -f {{.State.Running}} $CONTAINER_NAME-$NEW_COLOR)
    if [ "$is_new_up" != "true" ]; then
        echo "New container initialization failed."
        remove_new_container_and_image
        exit 1
    fi
}

replace_nginx_config() {
    cp $NGINX_PATH/nginx-$NEW_COLOR.conf $NGINX_PATH/nginx.conf
    docker exec dailyquest-client nginx -s reload
}

remove_previous_container() {
    # 컨테이너가 존재하지 않아도 스크립트가 중단되지 않음
    docker stop $CONTAINER_NAME-$COLOR
    docker rm $CONTAINER_NAME-$COLOR
}

remove_previous_image() {
    # 이미지가 존재하지 않으면 스크립트 중단됨
    # 기존 이미지가 존재할 때만 제거
    local previous_image_exist=$(docker images -f "reference=$CONTAINER_NAME:$COLOR" -q)
    if [ -n "$previous_image_exist" ]; then
        docker rmi $CONTAINER_NAME:$COLOR
    fi
}

# 함수 호출
resolve_new_color
replace_docker_image
start_new_container
validate_new_container
replace_nginx_config
remove_previous_container
remove_previous_image