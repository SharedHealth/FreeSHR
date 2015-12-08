package org.freeshr.util;


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.springframework.cassandra.core.CqlOperations;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.junit.Assert.assertEquals;

public class QueryUtils {

    private CqlOperations cqlOperations;

    public QueryUtils(CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public void insertEncByPatient(String encounterId, String healthId, Date createdAt) {
        Insert insert = QueryBuilder.insertInto("freeshr", "enc_by_patient").value("encounter_id", encounterId).value("health_id", healthId).value("created_at", TimeUuidUtil.uuidForDate(createdAt));
        cqlOperations.execute(insert);
    }

    public Row fetchEncounter(String encounterId) {
        ResultSet rs = cqlOperations.query(QueryBuilder.select().all().from("freeshr", "encounter").where(eq("encounter_id", encounterId)).limit(1));
        return rs.all().get(0);
    }

    public List<Row> fetchEncounterByPatientFeed(String healthId) {
        Select select = QueryBuilder.select().all().from("freeshr", "enc_by_patient");
        select.where(eq("health_id", healthId));
        ResultSet rs = cqlOperations.query(select);
        return rs.all();
    }

    public List<Row> fetchCatchmentFeed(String divisionId, String concatenatedDistrictId, int year) {
        Select select = QueryBuilder.select().all()
                .from("freeshr", "enc_by_catchment");
        select.where(eq("division_id", divisionId))
                .and(eq("district_id", concatenatedDistrictId))
                .and(eq("year", year));
        ResultSet rs = cqlOperations.query(select);

        return rs.all();
    }

    public void insertEncounter(String encounterId, String healthId, Date recievedAt, String content, String contentColumnName) {
        Insert insert = QueryBuilder.insertInto("freeshr", "encounter").value("encounter_id", encounterId).value("health_id", healthId).value("received_at", TimeUuidUtil.uuidForDate(recievedAt)).value(contentColumnName, content);
        cqlOperations.execute(insert);
    }

    public void insertEncounterByCatchment(String encounterId, String divisionId, String concatenatedDistrictId, String concatenatedUpazillaId, Date createdAt) {
        Insert insert = QueryBuilder.insertInto("freeshr", "enc_by_catchment")
                .value("encounter_id", encounterId).value("division_id", divisionId)
                .value("district_id", concatenatedDistrictId)
                .value("upazila_id", concatenatedUpazillaId)
                .value("year", DateUtil.getYearOf(createdAt))
                .value("created_at", TimeUuidUtil.uuidForDate(createdAt));
        cqlOperations.execute(insert);
    }


    public void insertPatient(String healthId) {
        Insert insert = QueryBuilder.insertInto("freeshr", "patient").value("health_id", healthId).value("active", true);
        cqlOperations.execute(insert);
    }

    public Row fetchPatient(String healthId) {
        ResultSet rs = cqlOperations.query(QueryBuilder.select().all().from("freeshr", "patient").where(eq("health_id", healthId)).limit(1));
        return rs.all().get(0);
    }

    public void trucateAllTables() {
        cqlOperations.execute("truncate encounter");
        cqlOperations.execute("truncate enc_by_catchment");
        cqlOperations.execute("truncate enc_by_patient");
        cqlOperations.execute("truncate enc_history");
        cqlOperations.execute("truncate patient");
    }

//    public void assertEncounter(EncounterBundle encounterBundle, String encounterId, String healthId, String content) {
//        assertEquals(encounterId, encounterBundle.getEncounterId());
//        assertEquals(healthId, encounterBundle.getHealthId());
//        assertEquals(content, encounterBundle.getEncounterContent());
//    }

    public void assertEncounterRow(Row encounterRow, String encounterId, String healthId, String content, String confidentiality) {
        assertEquals(encounterId, encounterRow.getString("encounter_id"));
        assertEquals(healthId, encounterRow.getString("health_id"));
        assertEquals(content, encounterRow.getString("content_v1"));
        assertEquals(confidentiality, encounterRow.getString("patient_confidentiality"));
    }

    public void assertEncounterByCatchmentRow(Row encounterByCatchmentRow, HashMap<String, String> expectedEncounterByCatchment) {
        assertEquals(expectedEncounterByCatchment.get("encounter_id"), encounterByCatchmentRow.getString("encounter_id"));
        assertEquals(expectedEncounterByCatchment.get("division_id"), encounterByCatchmentRow.getString("division_id"));
        assertEquals(expectedEncounterByCatchment.get("district_id"), encounterByCatchmentRow.getString("district_id"));
        assertEquals(expectedEncounterByCatchment.get("upazila_id"), encounterByCatchmentRow.getString("upazila_id"));
        assertEquals(Integer.valueOf(expectedEncounterByCatchment.get("year")).intValue(), encounterByCatchmentRow.getInt("year"));
        assertEquals(expectedEncounterByCatchment.get("created_at"), encounterByCatchmentRow.getUUID("created_at").toString());

        String expectedMergedAt = expectedEncounterByCatchment.get("merged_at");
        if (expectedMergedAt != null)
            assertEquals(expectedMergedAt, encounterByCatchmentRow.getUUID("merged_at").toString());
    }
}
