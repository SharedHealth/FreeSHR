CREATE TABLE if not exists FACILITIES (
    facility_id varchar primary key,
    facility_name varchar,
    facility_type varchar,
    catchments varchar,
    division_id varchar,
    district_id varchar,
    upazila_id varchar,
    city_corporation_id varchar,
    union_urban_ward_id varchar
);