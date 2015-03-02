CREATE TABLE enc_by_patient (
health_id varchar,
received_date timeuuid,
encounter_id varchar,
PRIMARY KEY (health_id, received_date) );
