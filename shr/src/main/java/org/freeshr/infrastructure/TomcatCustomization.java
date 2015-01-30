package org.freeshr.infrastructure;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TomcatCustomization implements TomcatConnectorCustomizer {
    @Override
    public void customize(Connector connector) {
        String shrMimeTypes = String.format("%s,%s,%s", MediaType.APPLICATION_JSON_VALUE, MediaType
                .APPLICATION_ATOM_XML_VALUE, MediaType.APPLICATION_XML_VALUE);
        connector.setProperty("compression", "on");
        connector.setProperty("compressableMimeType", shrMimeTypes);
    }
}
