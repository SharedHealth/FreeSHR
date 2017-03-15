package org.freeshr.application.fhir;


import org.apache.commons.lang.StringUtils;
import org.freeshr.utils.CollectionUtils;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.Resource;

import java.util.List;

import static org.freeshr.utils.CollectionUtils.isEvery;
import static org.freeshr.utils.CollectionUtils.isNotEmpty;

public class EncounterFunctions {

    public static final CollectionUtils.Fn<Condition, Boolean> isDiagnosis = new CollectionUtils.Fn<Condition, Boolean>() {
        @Override
        public Boolean call(Condition resource) {
            List<Coding> coding = resource.getCategoryFirstRep().getCoding();

            return isNotEmpty(coding) && isEvery(coding, new CollectionUtils.Fn<Coding, Boolean>() {
                @Override
                public Boolean call(Coding input) {
                    return input.getCode().equals("Diagnosis");
                }
            });
        }
    };

    public static final CollectionUtils.Fn<Coding, Boolean> hasSystem = new CollectionUtils.Fn<Coding, Boolean>() {
        @Override
        public Boolean call(Coding coding) {
            return coding.getSystem() != null && StringUtils.isNotEmpty(coding.getSystem());
        }
    };

    public static final CollectionUtils.Fn<Resource, Boolean> isCondition = new CollectionUtils.Fn<Resource, Boolean>() {
        @Override
        public Boolean call(Resource resource) {
            return resource instanceof Condition;
        }
    };

}
