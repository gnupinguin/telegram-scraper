## one-extractor

create dump:
docker exec -t resources_onoTlg_1 pg_dumpall -c -U onetlg > /dump_`date +%d-%m-%Y"_"%H_%M_%S`.sql

connect to db:
docker exec -it resources_onoTlg_1 psql -U onetlg onedb

Tor installation:
https://help.ubuntu.ru/wiki/tor

Tor change node:
https://stackoverflow.com/questions/1969958/how-to-change-the-tor-exit-node-programmatically-to-get-a-new-ip