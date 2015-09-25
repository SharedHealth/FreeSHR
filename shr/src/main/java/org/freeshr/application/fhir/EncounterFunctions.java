package org.freeshr.application.fhir;


import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.Condition;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static org.freeshr.utils.CollectionUtils.*;

public class EncounterFunctions {

    public static final Fn<Condition, Boolean> isDiagnosis = new Fn<Condition, Boolean>() {
        @Override
        public Boolean call(Condition resource) {
            List<CodingDt> coding = resource.getCategory().getCoding();

            return isNotEmpty(coding) && isEvery(coding, new Fn<CodingDt, Boolean>() {
                @Override
                public Boolean call(CodingDt input) {
                    return input.getCode().equals("Diagnosis");
                }
            });
        }
    };

    public static final Fn<CodingDt, Boolean> hasSystem = new Fn<CodingDt, Boolean>() {
        @Override
        public Boolean call(CodingDt coding) {
            return coding.getSystem() != null && StringUtils.isNotEmpty(coding.getSystem());
        }
    };

    public static final Fn<IResource, Boolean> isCondition = new Fn<IResource, Boolean>() {
        @Override
        public Boolean call(IResource resource) {
            return resource instanceof Condition;
        }
    };

//    public static final Fn<AtomEntry<? extends IResource>, IResource> toResource = new Fn<AtomEntry<? extends
//            IResource>, IResource>() {
//        @Override
//        public IResource call(AtomEntry<? extends IResource> input) {
//            return input.getResource();
//        }
//    };

}
