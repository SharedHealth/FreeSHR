package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class TerminologyServer {

    private SHRProperties shrProperties;
    private List<CodeValidator> codeValidatorList;

    @Autowired
    public TerminologyServer(SHRProperties shrProperties, List<CodeValidator> codeValidatorList) {
        this.shrProperties = shrProperties;
        this.codeValidatorList = codeValidatorList;
    }

    public Observable<Boolean> isValid(String system, String code) {
        String trServerReferencePath = StringUtils.ensureSuffix(shrProperties.getTerminologyServerReferencePath(), "/");
        String trLocationPath = StringUtils.ensureSuffix(shrProperties.getTRLocationPath(), "/");
        String terminologyRefSystem = null;
        try {
            URI systemUri = new URI(system);
            URI trServerUri = new URI(trServerReferencePath);
            if (!(systemUri.getHost().equals(trServerUri.getHost()) && systemUri.getPort() == trServerUri.getPort())) {
                return Observable.just(false);
            }
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(trLocationPath);
            uriBuilder.path(systemUri.getPath());
            uriBuilder.query(systemUri.getQuery());
            terminologyRefSystem = uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            Observable.just(false);
        }

        CodeValidator validator = identifyCodeValidator(terminologyRefSystem);
        if (validator != null) {
            return validator.isValid(terminologyRefSystem, code);
        }
        return Observable.just(false);
    }

    public boolean verifiesSystem(String system) {
        return identifyCodeValidator(system) != null;
    }

    private CodeValidator identifyCodeValidator(String system) {
        for (CodeValidator codeValidator : codeValidatorList) {
            if (codeValidator.supports(system)) {
                return codeValidator;
            }
        }
        return null;
    }
}
