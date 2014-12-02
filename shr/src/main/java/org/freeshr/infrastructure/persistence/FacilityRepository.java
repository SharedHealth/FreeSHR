package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.model.patient.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

@Component
public class FacilityRepository {
    private Logger logger = Logger.getLogger(FacilityRepository.class);
    private CqlTemplate cqlTemplate;

    @Autowired
    public FacilityRepository(@Qualifier("SHRCassandraTemplate") CqlTemplate cqlOperations) {
        this.cqlTemplate = cqlOperations;
    }

    public Observable<Facility> find(String facilityId) {
        String[] columns = new String[]{
                "facility_id", "facility_name", "facility_type", "catchments",
                "division_id", "district_id", "upazila_id", "city_corporation_id", "union_urban_ward_id"

        };

        Statement select = QueryBuilder.select(columns)
                .from("facilities")
                .where(QueryBuilder.eq("facility_id", facilityId));

        Observable<ResultSet> resultSet = Observable.from(cqlTemplate.queryAsynchronously(select.toString()), Schedulers.io());

        return resultSet.flatMap(new Func1<ResultSet, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(ResultSet rows) {
                Row facilityRow = rows.one();
                if(facilityRow == null) return Observable.just(null);
                    return Observable.just(read(facilityRow));
            }
        }, new Func1<Throwable, Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call(Throwable throwable) {
                logger.error(throwable.getMessage());
                return Observable.error(throwable);
            }
        }, new Func0<Observable<? extends Facility>>() {
            @Override
            public Observable<? extends Facility> call() {
                return null;
            }
        });
    }

    private Facility read(Row result) {
        Facility facility = new Facility();
        facility.setFacilityId(result.getString("facility_id"));
        facility.setFacilityName(result.getString("facility_name"));
        facility.setFacilityType(result.getString("facility_type"));
        facility.setCatchments(result.getString("catchments"));
        Address address = new Address();
        address.setDivision(result.getString("division_id"));
        address.setDistrict(result.getString("district_id"));
        address.setUpazila(result.getString("upazila_id"));
        address.setCityCorporation(result.getString("city_corporation_id"));
        address.setWard(result.getString("union_urban_ward_id"));
        facility.setFacilityLocation(address);
        return facility;
    }

    public Observable<Facility> save(final Facility facility) {
        Insert insert = buildInsertStatement(facility);

        Observable<ResultSet> saveObservable = Observable.from(cqlTemplate.executeAsynchronously(insert));

        return saveObservable.flatMap(new Func1<ResultSet, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(ResultSet rows) {
                return Observable.just(facility);
            }
        }, new Func1<Throwable, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(Throwable throwable) {
                logger.error(throwable.getMessage());
                return Observable.error(throwable);
            }
        }, new Func0<Observable<Facility>>() {
            @Override
            public Observable<Facility> call() {
                return Observable.just(facility);
            }
        });
    }

    private Insert buildInsertStatement(Facility facility) {
        return QueryBuilder
                .insertInto("facilities")
                .using(QueryBuilder.ttl(SHRProperties.ONE_DAY))
                .value("facility_id", facility.getFacilityId())
                .value("facility_name", facility.getFacilityName())
                .value("facility_type", facility.getFacilityType())
                .value("division_id", facility.getFacilityLocation().getDivision())
                .value("district_id", facility.getFacilityLocation().getDistrict())
                .value("upazila_id", facility.getFacilityLocation().getUpazila())
                .value("city_corporation_id", facility.getFacilityLocation().getCityCorporation())
                .value("union_urban_ward_id", facility.getFacilityLocation().getWard())
                .value("catchments", facility.getCatchmentsAsCommaSeparatedString())
                ;
    }

}
