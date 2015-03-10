package org.freeshr.infrastructure;

import org.apache.log4j.Logger;
import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.Facility;
import org.freeshr.interfaces.encounter.ws.FacilityMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import static org.freeshr.utils.HttpUtil.getSHRIdentityHeaders;


@Component
public class FacilityRegistryClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;
    private Logger logger = Logger.getLogger(FacilityRegistryClient.class);

    @Autowired
    public FacilityRegistryClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                                  SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrRestTemplate.getMessageConverters().add(0, new FacilityMessageConverter());
        this.shrProperties = shrProperties;
    }


    public Observable<Facility> getFacility(final String facilityId) {

        Observable<ResponseEntity<Facility>> response = Observable.from(shrRestTemplate.exchange(
                getFacilityUrl(facilityId),
                HttpMethod.GET,
                new HttpEntity(getSHRIdentityHeaders(shrProperties)),
                Facility.class));

        return response.map(parseResponse());
    }

    private Func1<ResponseEntity<Facility>, Facility> parseResponse() {
        return new Func1<ResponseEntity<Facility>, Facility>() {
            @Override
            public Facility call(ResponseEntity<Facility> facilityResponseEntity) {
                return facilityResponseEntity.getBody();
            }
        };
    }

    private String getFacilityUrl(String facilityId) {
        return shrProperties.getFacilityRegistryUrl() + facilityId + ".json";
    }
}
