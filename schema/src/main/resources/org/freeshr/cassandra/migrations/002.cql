create table if not exists encounter (
 encounter_id varchar,
 health_id varchar,
 received_at timeuuid,
 patient_confidentiality varchar,
 encounter_confidentiality varchar,
 content_version int,
 content_version_v1 int,
 content_v1 varchar,
 created_by varchar,
 updated_at timeuuid,
 updated_by varchar,
 PRIMARY KEY(encounter_id, received_at));
