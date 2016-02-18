package org.freeshr.util;


import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import org.freeshr.utils.DateUtil;
import org.freeshr.utils.TimeUuidUtil;
import org.springframework.cassandra.core.CqlOperations;

import java.util.Date;

public class QueryUtils {

    private CqlOperations cqlOperations;

    public QueryUtils(CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public void insertEncounterByCatchment(String encounterId, String divisionId, String concatenatedDistrictId, String concatenatedUpazillaId, Date createdAt) {
        Insert insert = QueryBuilder.insertInto("freeshr", "enc_by_catchment")
                .value("encounter_id", encounterId).value("division_id", divisionId)
                .value("district_id", concatenatedDistrictId)
                .value("upazila_id", concatenatedUpazillaId)
                .value("year", DateUtil.getYearOf(createdAt))
                .value("created_at", TimeUUIDUtils.getTimeUUID(createdAt.getTime()));
        cqlOperations.execute(insert);
    }
}
