CREATE TABLE if not exists enc_history (
encounter_id varchar,
encounter_updated_at timeuuid,
content_version int,
content_format varchar,
content varchar,
PRIMARY KEY (encounter_id, encounter_updated_at));
