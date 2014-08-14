package org.freeshr.infrastructure.tr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.freeshr.utils.CollectionUtils.toSet;

@Component
public class Hl7CodeValidator implements CodeValidator {

    private final Map<String, Set<String>> hl7Codes = new HashMap<String, Set<String>>();


    @Autowired
    public Hl7CodeValidator(@Qualifier("hl7CodeProperties") Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            hl7Codes.put(entry.getKey().toString(), toSet(entry.getValue().toString(), ","));
        }
    }

    public org.springframework.util.concurrent.ListenableFuture<Boolean> isValid(String uri, String code) {
        final SettableListenableFuture<Boolean> future = new SettableListenableFuture<Boolean>();
        future.set(hl7Codes.containsKey(uri) && hl7Codes.get(uri).contains(lowerCase(code)));
        return future;
    }


}
