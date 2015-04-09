create table encounter (
 encounter_id varchar,
 health_id varchar,
 received_date timestamp,
 patient_location_code varchar,
 patient_confidentiality varchar,
 encounter_confidentiality varchar,
 content_version int,
 content_version_v1 int,
 content_v1 varchar,
 created_by varchar,
 updated_date timestamp,
 updated_by varchar,
 PRIMARY KEY(encounter_id, received_date));
