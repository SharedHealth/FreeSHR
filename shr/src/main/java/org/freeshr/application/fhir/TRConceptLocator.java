package org.freeshr.application.fhir;

import org.apache.commons.lang.StringUtils;
import org.freeshr.config.SHRProperties;
import org.hl7.fhir.instance.model.Code;
import org.hl7.fhir.instance.model.OperationOutcome;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.utils.ConceptLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.freeshr.utils.CollectionUtils.fetch;
import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

@Component
public class TRConceptLocator implements ConceptLocator {

    private String path;
    private final AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public TRConceptLocator(String path, AsyncRestTemplate shrRestTemplate, SHRProperties shrProperties) {
        this.path = path;
        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    @Override
    public ValueSet.ValueSetDefineConceptComponent locate(String system, final String code) {
        final SettableListenableFuture<Boolean> future = getConcept(system, code);
        try {
            if (future.get()) {
                ValueSet.ValueSetDefineConceptComponent result = new ValueSet.ValueSetDefineConceptComponent();
                Code resultCode = new Code();
                resultCode.setValue(code);
                result.setCode(resultCode);
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ValidationResult validate(String system, String code, String display) {
        final SettableListenableFuture<Boolean> future = getConcept(system, code);
        ValidationResult errorResult = new ValidationResult(OperationOutcome.IssueSeverity.error, "The concept with the given code was not found");
        try {
            if (future.get()) {
                return new ValidationResult(OperationOutcome.IssueSeverity.Null, StringUtils.EMPTY);
            } else {
                return errorResult;
            }
        } catch (Exception e) {
            return errorResult;
        }
    }

    @Override
    public boolean verifiesSystem(String system) {
        return StringUtils.contains(system, "openmrs");
    }

    @Override
    public List<ValueSet.ValueSetExpansionContainsComponent> expand(ValueSet.ConceptSetComponent inc) throws Exception {
        return Collections.EMPTY_LIST;
    }

    private SettableListenableFuture<Boolean> getConcept(String system, final String code) {
        final SettableListenableFuture<Boolean> future = new SettableListenableFuture<Boolean>();
        get(system, new ListenableFutureCallback<ResponseEntity<Map>>() {
            public void onSuccess(ResponseEntity<Map> result) {
                future.set(result.getStatusCode().is2xxSuccessful() && fetch(result.getBody(), path).equals(code));
            }

            public void onFailure(Throwable t) {
                future.set(false);
            }
        });
        return future;
    }

    private void get(String uri, ListenableFutureCallback<ResponseEntity<Map>> callback) {
        ListenableFuture<ResponseEntity<Map>> future = shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                Map.class);
        future.addCallback(callback);
    }
}
