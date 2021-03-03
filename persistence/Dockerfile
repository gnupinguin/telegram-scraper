FROM postgres:latest

ENV POSTGRES_USER scraper
ENV POSTGRES_PASSWORD Scraper_db!
ENV POSTGRES_DB scraperdb

COPY src/main/resources/schema/*.sql /docker-entrypoint-initdb.d/