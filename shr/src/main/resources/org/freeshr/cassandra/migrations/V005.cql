drop table encounter;
create table encounter (encounter_id varchar , health_id varchar, date timestamp, content varchar,district_id varchar,division_id varchar,upazilla_id varchar,city_corporation_id varchar,ward_id varchar ,PRIMARY KEY(encounter_id,date));

CREATE INDEX idx_health_id ON encounter (health_id);
CREATE INDEX encounter_address_division_index ON encounter (division_id);
CREATE INDEX encounter_address_district_index ON encounter (district_id);
CREATE INDEX encounter_address_upazilla_index ON encounter (upazilla_id);
CREATE INDEX encounter_date_time_index ON encounter (date);