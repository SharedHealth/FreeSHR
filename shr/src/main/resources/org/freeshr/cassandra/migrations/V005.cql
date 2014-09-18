alter table encounter add division_id varchar;
alter table encounter add district_id varchar;
alter table encounter add upazilla_id varchar;
alter table encounter add city_corporation_id varchar;
alter table encounter add ward_id varchar;
CREATE INDEX encounter_address_division_index ON encounter (division_id);
CREATE INDEX encounter_address_district_index ON encounter (district_id);
CREATE INDEX encounter_address_upazilla_index ON encounter (upazilla_id);