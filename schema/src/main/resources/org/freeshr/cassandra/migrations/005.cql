CREATE TABLE if not exists enc_by_patient (
health_id varchar,
created_at timeuuid,
encounter_id varchar,
PRIMARY KEY (health_id, created_at));
