package org.freeshr.infrastructure.mci;

import org.freeshr.config.SHRProperties;
import org.freeshr.domain.model.patient.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import static org.freeshr.utils.HttpUtil.*;

@Component
public class MCIClient {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public MCIClient(@Qualifier("SHRRestTemplate") AsyncRestTemplate shrRestTemplate,
                     SHRProperties shrProperties) {
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    public Observable<Patient> getPatient(String healthId, String clientId, String userEmail, String accessToken) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add(AUTH_TOKEN_KEY, accessToken);
        headers.add(CLIENT_ID_KEY, clientId);
        headers.add(FROM_KEY, userEmail);

        Observable<ResponseEntity<Patient>> responseEntityObservable = Observable.from(shrRestTemplate.exchange(
                shrProperties.getMCIPatientUrl() + "/" + healthId,
                HttpMethod.GET,
                new HttpEntity(headers),
                Patient.class));

        return responseEntityObservable.map(new Func1<ResponseEntity<Patient>, Patient>() {
            @Override
            public Patient call(ResponseEntity<Patient> patientResponse) {
                if (patientResponse.getStatusCode().is2xxSuccessful()) {
                    return patientResponse.getBody();
                } else {
                    return null;
                }
            }
        });
    }

}
