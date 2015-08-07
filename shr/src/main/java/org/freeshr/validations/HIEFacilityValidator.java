package org.freeshr.validations;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.domain.service.FacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.freeshr.utils.UrlUtil.extractFacilityId;

@Component
public class HIEFacilityValidator {
    
    private SHRProperties shrProperties;
    private FacilityService facilityService;

    @Autowired
    public HIEFacilityValidator(SHRProperties shrProperties, FacilityService facilityService) {
        this.shrProperties = shrProperties;
        this.facilityService = facilityService;
    }

    public Boolean validate (String url){
        if (url.isEmpty() || !isValidFacilityUrl(url)) {
            return false;
        }

        Facility facility = facilityService.checkForFacility(extractFacilityId(url)).toBlocking().first();
        if (facility == null) {
            return false;
        }
        return true;
    }

    private boolean isValidFacilityUrl(String referenceSimple) {
        String facilityRegistryUrl = shrProperties.getFacilityReferencePath();
        return referenceSimple.contains(facilityRegistryUrl);
    }
}
