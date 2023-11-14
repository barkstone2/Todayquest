REDIS_STATUS=$(systemctl is-active redis6)

if [ "$REDIS_STATUS" != "active" ]; then
    systemctl restart redis6
fi
