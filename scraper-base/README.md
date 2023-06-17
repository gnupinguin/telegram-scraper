## one-extractor

create dump:
docker exec -t resources_onoTlg_1 pg_dumpall -c -U scraper > /dump.sql

connect to db:
docker exec -it dumps_scraper_1 psql -U scraper scraperdb

import dump:


Tor installation:
https://help.ubuntu.ru/wiki/tor

Tor change node:
https://stackoverflow.com/questions/1969958/how-to-change-the-tor-exit-node-programmatically-to-get-a-new-ip