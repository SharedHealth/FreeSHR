package org.freeshr.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.freeshr.application.fhir.EncounterBundle;
import org.freeshr.domain.model.Catchment;
import org.freeshr.events.EncounterEvent;
import org.freeshr.infrastructure.persistence.EncounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CatchmentEncounterService {
    private static final int DEFAULT_FETCH_LIMIT = 20;
    private static final String ENCOUNTER_FETCH_LIMIT_LOOKUP_KEY = "ENCOUNTER_FETCH_LIMIT";
    private EncounterRepository encounterRepository;

    @Autowired
    public CatchmentEncounterService(EncounterRepository encounterRepository) {
        this.encounterRepository = encounterRepository;
    }

    public Observable<List<EncounterBundle>> findEncountersForFacilityCatchment(final String catchment,
                                                                                final Date sinceDate, final int limit) {
        return encounterRepository.findEncountersForCatchment(new Catchment(catchment), sinceDate, limit);
    }

    public Observable<List<EncounterEvent>> findEncounterFeedForFacilityCatchment(final String catchment,
                                                                                  final Date sinceDate, final int limit) {
        return encounterRepository.findEncounterFeedForCatchment(new Catchment(catchment), sinceDate, limit);
    }

    public static int getEncounterFetchLimit() {
        Map<String, String> env = System.getenv();
        String encounterFetchLimit = env.get(ENCOUNTER_FETCH_LIMIT_LOOKUP_KEY);
        int fetchLimit = DEFAULT_FETCH_LIMIT;
        if (!StringUtils.isBlank(encounterFetchLimit)) {
            try {
                fetchLimit = Integer.valueOf(encounterFetchLimit);
            } catch (NumberFormatException nfe) {
                //Do nothing
            }
        }
        return fetchLimit;
    }
}
