DROP TABLE IF EXISTS hashtag;
CREATE TABLE hashtag(
   id BIGSERIAL,
   internal_message_id bigint NOT NULL,
   tag varchar(128) NOT NULL,
   PRIMARY KEY(id)
);

DROP TABLE IF EXISTS link;
CREATE TABLE link(
   id BIGSERIAL,
   internal_message_id bigint NOT NULL,
   url varchar(2048) NOT NULL,
   PRIMARY KEY(id)
);


DROP TABLE IF EXISTS mention;
CREATE TABLE mention(
   id BIGSERIAL,
   chat_name varchar(255) NOT NULL, --without constraints, because we still can read the chat
   internal_message_id bigint NOT NULL,
   PRIMARY KEY(id)
);

-- CREATE TYPE message_content_type AS ENUM ('Text', 'Photo','Video','Audio','Document','Other');

-- forwarded_from_message_id currently without constraints, because we do not use it for scrapping
DROP TABLE IF EXISTS message;
CREATE TABLE message(
   internal_id BIGSERIAL, --TODO check removing field for normalization process
   chat_id bigint NOT NULL,
   message_id bigint NOT NULL,
   reply_to_message_id bigint,
   forwarded_from_chat_id bigint, --TODO extract to table forwarding
   forwarded_from_message_id bigint,
   type int2 NOT NULL,
   text_content varchar(4096),
   publish_date timestamp NOT NULL,
   load_date timestamp NOT NULL,
   views int NOT NULL,
   PRIMARY KEY(internal_id)
);

DROP TABLE IF EXISTS chat;
CREATE TABLE chat(
    name varchar(32) NOT NULL,
    id bigint,
    description varchar(256),
    members int NOT NULL,
    PRIMARY KEY(name)
);

ALTER TABLE chat ADD CONSTRAINT uk_chat_id UNIQUE(id);

ALTER TABLE message ADD CONSTRAINT fk_message_chat_id FOREIGN KEY (chat_id)
    REFERENCES chat (id);
ALTER TABLE message ADD CONSTRAINT fk_forwarded_from_chat_id FOREIGN KEY (chat_id)
    REFERENCES chat (id);
ALTER TABLE message ADD CONSTRAINT uk_chat_message_id UNIQUE(chat_id, message_id);
CREATE INDEX message_date_idx ON message(publish_date);

ALTER TABLE hashtag ADD CONSTRAINT fk_hashtag_message_id FOREIGN KEY (internal_message_id)
    REFERENCES message (internal_id);

ALTER TABLE link ADD CONSTRAINT fk_link_message_id FOREIGN KEY (internal_message_id)
    REFERENCES message (internal_id);


ALTER TABLE mention ADD CONSTRAINT fk_mention_message_id FOREIGN KEY (internal_message_id)
    REFERENCES message (internal_id);
CREATE INDEX mention_chat_name_idx ON mention(chat_name);
