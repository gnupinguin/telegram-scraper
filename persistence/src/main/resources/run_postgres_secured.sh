#!/bin/bash

# TODO ADD Volume and data save run, (builder-flow)

# locally
# docker run --name mypostgres -p 5433:5432 -e POSTGRES_PASSWORD=pass -d postgres

# DO NOT FORGET CHMOD 777 on volume files, otherwise it will not work!!!
sudo chmod -R 777 $HOME/volume
docker-compose up -d
