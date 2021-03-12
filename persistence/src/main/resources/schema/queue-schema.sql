DROP TABLE IF EXISTS mention_queue;
CREATE TABLE mention_queue(
                              name varchar(32),
                              status integer not null default 0, -- 0 - not processed, 1 - start processing, 2 - finished
                              PRIMARY KEY(name)
);

CREATE INDEX mention_queue_status_idx on mention_queue (status);


INSERT INTO mention_queue(name) VALUES ('rian_ru');
INSERT INTO mention_queue(name) VALUES ('novosty');
INSERT INTO mention_queue(name) VALUES ('topor');
INSERT INTO mention_queue(name) VALUES ('meduzalive');
INSERT INTO mention_queue(name) VALUES ('meduzaevening');
INSERT INTO mention_queue(name) VALUES ('navalny');
INSERT INTO mention_queue(name) VALUES ('politika');
INSERT INTO mention_queue(name) VALUES ('russica2');
INSERT INTO mention_queue(name) VALUES ('russicatop');
INSERT INTO mention_queue(name) VALUES ('proeconomics');
INSERT INTO mention_queue(name) VALUES ('mediatech');
INSERT INTO mention_queue(name) VALUES ('redzion');
INSERT INTO mention_queue(name) VALUES ('incnews');
INSERT INTO mention_queue(name) VALUES ('tolk_tolk');
INSERT INTO mention_queue(name) VALUES ('scienpolicy');
INSERT INTO mention_queue(name) VALUES ('actual_comment');
INSERT INTO mention_queue(name) VALUES ('wciomofficial');
INSERT INTO mention_queue(name) VALUES ('birmanalex');
INSERT INTO mention_queue(name) VALUES ('karaulny');
INSERT INTO mention_queue(name) VALUES ('malzoffsgallery');
INSERT INTO mention_queue(name) VALUES ('cyberleninka');
INSERT INTO mention_queue(name) VALUES ('breakingmash');
INSERT INTO mention_queue(name) VALUES ('ru_ftp');
INSERT INTO mention_queue(name) VALUES ('lentachold');
INSERT INTO mention_queue(name) VALUES ('mrzlkvk');
INSERT INTO mention_queue(name) VALUES ('varlamov_news');
INSERT INTO mention_queue(name) VALUES ('directorate4');
INSERT INTO mention_queue(name) VALUES ('rospres');
INSERT INTO mention_queue(name) VALUES ('davydovin');
INSERT INTO mention_queue(name) VALUES ('kremlin_mother_expert');
INSERT INTO mention_queue(name) VALUES ('mislinemisli');
INSERT INTO mention_queue(name) VALUES ('metodi4ka');
INSERT INTO mention_queue(name) VALUES ('politjoystic');
INSERT INTO mention_queue(name) VALUES ('kbrvdvkr');
INSERT INTO mention_queue(name) VALUES ('polittemnik');
INSERT INTO mention_queue(name) VALUES ('shadow_policy');
INSERT INTO mention_queue(name) VALUES ('terzdrsm');
INSERT INTO mention_queue(name) VALUES ('radiogovoritmsk');
INSERT INTO mention_queue(name) VALUES ('boilerroomchannel');
INSERT INTO mention_queue(name) VALUES ('obrazbuduschego');
INSERT INTO mention_queue(name) VALUES ('kononenkome');
INSERT INTO mention_queue(name) VALUES ('rt_russian');
INSERT INTO mention_queue(name) VALUES ('temablog');
INSERT INTO mention_queue(name) VALUES ('rasstriga');
INSERT INTO mention_queue(name) VALUES ('kremlebezbashennik');
INSERT INTO mention_queue(name) VALUES ('antiskrepa');
INSERT INTO mention_queue(name) VALUES ('stalin_gulag');

