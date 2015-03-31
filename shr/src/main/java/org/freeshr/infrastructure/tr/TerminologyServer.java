package org.freeshr.infrastructure.tr;

import org.freeshr.config.SHRProperties;
import org.freeshr.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

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
        if (!system.startsWith(trServerReferencePath)) {
            return Observable.just(false);
        }

        String trLocationPath = StringUtils.ensureSuffix(shrProperties.getTRLocationPath(), "/");
        String terminologyRefSystem = system.replace(trServerReferencePath, trLocationPath);

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
