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
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import static org.freeshr.utils.HttpUtil.basicHeaders;
import static org.freeshr.utils.concurrent.FutureConverter.toListenableFuture;
import static org.freeshr.utils.concurrent.FutureConverter.toObservable;

@Component
public class FacilityRegistryClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;
    private Logger logger = Logger.getLogger(FacilityRegistryClient.class);

    @Autowired
    public FacilityRegistryClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrRestTemplate.getMessageConverters().add(0, new FacilityMessageConverter());
        this.shrProperties = shrProperties;
    }


    public ListenableFuture<Facility> getFacility(final String facilityId) {
        Observable<ResponseEntity<Facility>> response = toObservable(shrRestTemplate.exchange(
                getFacilityUrl(facilityId),
                HttpMethod.GET,
                new HttpEntity(basicHeaders(shrProperties.getFacilityRegistryAuthToken())),
                Facility.class));

        Observable<Facility> facilityObservable = response.flatMap(new Func1<ResponseEntity<Facility>, Observable<Facility>>() {
            @Override
            public Observable<Facility> call(ResponseEntity<Facility> result) {
                return Observable.from(result.getBody());
            }
        });

        return toListenableFuture(facilityObservable);
    }

    private String getFacilityUrl(String facilityId) {
        return shrProperties.getFacilityRegistryUrl() + facilityId + ".json";
    }
}
