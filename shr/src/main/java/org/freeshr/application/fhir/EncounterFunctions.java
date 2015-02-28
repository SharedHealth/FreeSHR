package org.freeshr.application.fhir;


import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Resource;

import java.util.List;

import static org.freeshr.utils.CollectionUtils.*;

public class EncounterFunctions {

    public static final Fn<Condition, Boolean> isDiagnosis = new Fn<Condition, Boolean>() {
        @Override
        public Boolean call(Condition resource) {
            List<Coding> coding = resource.getCategory().getCoding();

            return isNotEmpty(coding) && isEvery(coding, new Fn<Coding, Boolean>() {
                @Override
                public Boolean call(Coding input) {
                    return input.getCodeSimple().equals("Diagnosis");
                }
            });
        }
    };

    public static final Fn<Coding, Boolean> hasSystem = new Fn<Coding, Boolean>() {
        @Override
        public Boolean call(Coding coding) {
            return coding.getSystem() != null && StringUtils.isNotEmpty(coding.getSystem().getValue());
        }
    };

    public static final Fn<Resource, Boolean> isCondition = new Fn<Resource, Boolean>() {
        @Override
        public Boolean call(Resource resource) {
            return resource instanceof Condition;
        }
    };

    public static final Fn<AtomEntry<? extends Resource>, Resource> toResource = new Fn<AtomEntry<? extends
            Resource>, Resource>() {
        @Override
        public Resource call(AtomEntry<? extends Resource> input) {
            return input.getResource();
        }
    };

}
