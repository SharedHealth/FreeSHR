package org.freeshr.infrastructure.tr;


import org.apache.commons.io.IOUtils;
import org.freeshr.utils.FileUtil;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Extension;
import org.hl7.fhir.instance.model.Resource;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class ResourceExtensionTest {

    @Test
    @Ignore
    public void shouldDeserializeMedicationWithExtension() throws Exception {
        String medicationJson = FileUtil.asString("jsons/medication_extn.json");
        //TODO
//        Resource resource = new JsonParser().parse(IOUtils.toInputStream(medicationJson, "UTF-8"));
//
//        List<Extension> extensions = resource.getExtensions();
//        for (Extension extension : extensions) {
//            System.out.println(extension.getUrl().getValue());
//            String_ value = (String_) extension.getValue();
//            System.out.println(value.getValue());
//        }
//        Extension extension = resource.getExtension("http://192.168.33.17:9080/openmrs/ws/rest/v1/tr/medication#med-extension-strength");
//        System.out.println(extension);
    }
}
