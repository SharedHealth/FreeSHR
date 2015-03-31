package org.freeshr.infrastructure.tr;


import org.apache.commons.io.IOUtils;
import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestTemplate;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

import static org.freeshr.utils.HttpUtil.basicAuthHeaders;

@Component
public class ValueSetCodeValidator implements CodeValidator {

    private AsyncRestTemplate shrRestTemplate;
    private SHRProperties shrProperties;

    @Autowired
    public ValueSetCodeValidator(AsyncRestTemplate shrRestTemplate,
                                 SHRProperties shrProperties) {

        this.shrRestTemplate = shrRestTemplate;
        this.shrProperties = shrProperties;
    }

    @Override
    public boolean supports(String system) {
        return system != null && system.contains(shrProperties.getTerminologiesContextPathForValueSet());
    }

    @Override
    public Observable<Boolean> isValid(final String system, final String code) {
        Observable<Boolean> map = get(system).map(new Func1<ResponseEntity<String>, Boolean>() {
            @Override
            public Boolean call(ResponseEntity<String> stringResponseEntity) {
                try {
                    Resource resource = new JsonParser().parse(IOUtils.toInputStream(stringResponseEntity.getBody(),
                            "UTF-8"));
                    ValueSet valueSet = (ValueSet) resource;
                    ValueSet.ValueSetDefineComponent definition = valueSet.getDefine();
                    Boolean isCaseSensitive = definition.getCaseSensitive().getValue();

                    ConceptMatcher conceptMatcher = getConceptMatcher(isCaseSensitive);

                    List<ValueSet.ValueSetDefineConceptComponent> concepts = definition.getConcept();
                    for (ValueSet.ValueSetDefineConceptComponent concept : concepts) {
                        if (conceptMatcher.isMatching(concept.getCode().getValue(), code)) {
                            return true;
                        }
                    }
                    return false;


                } catch (Exception e) {
                    //TODO: should instead use future.setException()
                    return false;
                }
            }

        });
        return map.onErrorReturn(new Func1<Throwable, Boolean>() {
            @Override
            public Boolean call(Throwable throwable) {
                return false;
            }
        });
    }

    private interface ConceptMatcher {
        public boolean isMatching(String str1, String str2);
    }

    private ConceptMatcher getConceptMatcher(Boolean isCaseSensitive) {
        if (isCaseSensitive) return new ConceptMatcher() {
            @Override
            public boolean isMatching(String str1, String str2) {
                return str1.equals(str2);
            }
        };
        return new ConceptMatcher() {
            @Override
            public boolean isMatching(String str1, String str2) {
                return str1.equalsIgnoreCase(str2);
            }
        };
    }

    String formatUrl(String code) {
        return StringUtils.removeSuffix(shrProperties.getTRLocationPath(), "/") + shrProperties.getTerminologiesContextPathForValueSet() + code;
    }

    boolean shouldCreateUrl(String uri) {
        return !(uri.startsWith("http://") || uri.startsWith("https://"));
    }

    private Observable<ResponseEntity<String>> get(String uri) {
        return Observable.from(shrRestTemplate.exchange(uri,
                HttpMethod.GET,
                new HttpEntity(basicAuthHeaders(shrProperties.getTrUser(), shrProperties.getTrPassword())),
                String.class));
    }
}
