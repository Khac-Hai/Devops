#!/bin/bash

docker run -d --name rikkei-course-service -p 8081:80 nginxdemos/hello

docker ps
