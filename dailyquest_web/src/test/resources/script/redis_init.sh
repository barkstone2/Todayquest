#!/bin/bash

redis-cli -n 1 SADD npr pre1
redis-cli -n 1 SADD npr pre2
redis-cli -n 1 SADD npr pre3

redis-cli -n 1 SADD npo post1
redis-cli -n 1 SADD npo post2
redis-cli -n 1 SADD npo post3

redis-cli -n 1 HSET et 1 10
redis-cli -n 1 HSET et 2 20
redis-cli -n 1 HSET et 3 30
redis-cli -n 1 HSET et 4 0

redis-cli -n 1 HSET s qce 1
redis-cli -n 1 HSET s qcg 1
redis-cli -n 1 HSET s mrc 30