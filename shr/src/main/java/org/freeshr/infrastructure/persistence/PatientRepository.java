package org.freeshr.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.freeshr.domain.model.patient.Address;
import org.freeshr.domain.model.patient.Patient;
import org.freeshr.utils.Confidentiality;
import org.freeshr.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.freeshr.infrastructure.persistence.RxMaps.completeResponds;
import static org.freeshr.infrastructure.persistence.RxMaps.respondOnNext;

@Component
public class PatientRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("SHRCassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public Observable<Patient> find(String healthId) {
        Observable<ResultSet> observable = Observable.from(
                cqlOperations.queryAsynchronously("SELECT " +
                        " health_id, gender, division_id, district_id, upazila_id, city_corporation_id, " +
                        "union_urban_ward_id, address_line, confidentiality" +
                        " FROM patient WHERE health_id='" + healthId + "';"));
        return observable.map(new Func1<ResultSet, Patient>() {
            @Override
            public Patient call(ResultSet rows) {
                return readPatient(rows);
            }
        });
    }

    private Patient readPatient(ResultSet resultSet) {
        Row result = resultSet.one();
        if (null != result) {
            Patient patient = new Patient();
            Address address = new Address();
            patient.setHealthId(result.getString("health_id"));
            patient.setGender(result.getString("gender"));
            patient.setConfidentiality(isConfidential(result.getString("Confidentiality")));
            address.setDivision(result.getString("division_id"));
            address.setDistrict(result.getString("district_id"));
            address.setUpazila(result.getString("upazila_id"));
            address.setCityCorporation(result.getString("city_corporation_id"));
            address.setUnionOrUrbanWardId(result.getString("union_urban_ward_id"));
            address.setLine(result.getString("address_line"));
            patient.setAddress(address);
            return patient;
        } else {
            return null;
        }
    }

    private boolean isConfidential(String confidentiality) {
        if ((confidentiality == null) || "".equals(confidentiality)) {
            return false;
        } else {
            return confidentiality.equals("V");
        }
    }

    public Observable<Boolean> save(Patient patient) {
        Observable<ResultSet> saveObservable = Observable.from(
                cqlOperations.executeAsynchronously(buildPatientInsertQuery(patient)), Schedulers.io());
        return saveObservable.flatMap(respondOnNext(true), RxMaps.<Boolean>logAndForwardError(logger),
                completeResponds(true));
    }

    private Insert buildPatientInsertQuery(Patient patient) {
        Address address = patient.getAddress();
        return QueryBuilder.insertInto("patient")
                .value("health_id", patient.getHealthId())
                .value("gender", patient.getGender())
                .value("address_line", address.getLine())
                .value("division_id", address.getDivision())
                .value("district_id", address.getDistrict())
                .value("upazila_id", address.getUpazila())
                .value("city_corporation_id", address.getCityCorporation())
                .value("union_urban_ward_id", address.getUnionOrUrbanWardId())
                .value("confidentiality", patient.getConfidentiality().getLevel());
    }
}
