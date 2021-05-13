# telegram-scrapper
Java Telegram Channel Scrappers

## Goal
Collect Russian news from Telegram

### Configuration

#### External dependencies:
- Installed and [configured](https://stackoverflow.com/questions/1969958/how-to-change-the-tor-exit-node-programmatically-to-get-a-new-ip) Tor
- Configured database(PostgreSQL)(see persistence/src/main/resources/docker-compose.yml)

#### Scraper config
All scraper settings specified in scraper/src/main/resources/application.yml

You can enable bot notifications for scraper failures. Add the below code to config file:

``` 
telegram:
  bot:
    chatId: 123456
    token: 123456:AAA-BB-CC
```

### Data analysis

Collected data analysis present in branch ```analyzer``` and in future will be extracted to separate project.
Part of the future project is here: https://github.com/gnupinguin/spark-lda-coherence

## Run
```
git clone https://github.com/gnupinguin/telegram-scraper.git
cd telegram-scraper
./gradlew scraper:bootRun
```

