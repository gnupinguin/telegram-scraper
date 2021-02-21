FROM postgres:latest

ENV POSTGRES_USER onetlg
ENV POSTGRES_PASSWORD password
ENV POSTGRES_DB onedb

COPY src/main/resources/schema/* /docker-entrypoint-initdb.d/