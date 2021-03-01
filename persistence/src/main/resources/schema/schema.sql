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

DROP TABLE IF EXISTS forwarding;
CREATE TABLE forwarding(
                           internal_message_id bigint NOT NULL,
                           forwarded_from_channel varchar(32) NOT NULL,
                           forwarded_from_message_id bigint NOT NULL,
                           PRIMARY KEY(internal_message_id)
);

DROP TABLE IF EXISTS replying;
CREATE TABLE replying(
                         internal_message_id bigint NOT NULL,
                         reply_to_message_id bigint NOT NULL,
                         PRIMARY KEY(internal_message_id)
);

DROP TABLE IF EXISTS message;
CREATE TABLE message(
   internal_id BIGSERIAL,
   chat_id bigint NOT NULL,
   message_id bigint NOT NULL,
   type int2 NOT NULL,
   text_content varchar(4096),
   publish_date timestamp NOT NULL,
   load_date timestamp NOT NULL,
   views int NOT NULL,
   PRIMARY KEY(internal_id)
);

DROP TABLE IF EXISTS chat;
CREATE TABLE chat(
    id BIGSERIAL,
    name varchar(32) NOT NULL,
    title varchar(32),
    description varchar(256),
    members int NOT NULL,
    PRIMARY KEY(id)
);

ALTER TABLE chat ADD CONSTRAINT uk_chat_id UNIQUE(name);

ALTER TABLE message ADD CONSTRAINT fk_message_chat_id FOREIGN KEY (chat_id)
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

ALTER TABLE forwarding ADD CONSTRAINT fk_forwarding_message_id FOREIGN KEY (internal_message_id)
    REFERENCES message (internal_id);
ALTER TABLE replying ADD CONSTRAINT fk_replying_message_id FOREIGN KEY (internal_message_id)
    REFERENCES message (internal_id);