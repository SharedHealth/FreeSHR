create table encounter (encounter_id varchar PRIMARY KEY, health_id varchar, date varchar, content varchar);

CREATE INDEX idx_health_id ON encounter (health_id);