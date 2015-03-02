create table encounter (
 encounter_id varchar,
 health_id varchar,
 received_date timestamp,
 content varchar,
 patient_location_code varchar,
 PRIMARY KEY(encounter_id, received_date));

CREATE INDEX idx_encounter_patient_location_code ON encounter (patient_location_code);
CREATE INDEX idx_encounter_health_id ON encounter (health_id);
