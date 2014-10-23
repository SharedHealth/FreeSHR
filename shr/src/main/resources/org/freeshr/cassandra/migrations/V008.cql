CREATE TABLE enc_by_catchment (division_id varchar, district_id varchar, year int, received_date timeuuid, upazila_id varchar, city_corporation_id varchar, union_urban_ward_id varchar, encounter_id varchar, PRIMARY KEY ((division_id, district_id, year), received_date) );
CREATE INDEX idx_enc_by_catchment_upazila on enc_by_catchment(upazila_id);
CREATE INDEX idx_enc_by_catchment_corporation on enc_by_catchment(city_corporation_id);
CREATE INDEX idx_enc_by_catchment_union_ward on enc_by_catchment(union_urban_ward_id);