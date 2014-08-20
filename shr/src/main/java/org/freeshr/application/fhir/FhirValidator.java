package org.freeshr.application.fhir;


import org.hl7.fhir.instance.validation.InstanceValidator;
import org.hl7.fhir.instance.validation.ValidationErrorHandler;
import org.hl7.fhir.instance.validation.ValidationMessage;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.freeshr.utils.Lambda.throwIfNot;

public class FhirValidator {

    public void validate(String sourceXml, String definitionsZipPath) throws Exception {
        List<ValidationMessage> outputs = new ArrayList<ValidationMessage>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ValidationErrorHandler(outputs));
        Document doc = builder.parse(new ByteArrayInputStream(sourceXml.getBytes()));
        outputs.addAll(new InstanceValidator(readDefinitions(definitionsZipPath), null).validateInstance(doc.getDocumentElement()));
        throwIfNot(outputs.isEmpty(), new InvalidEncounter(new Error("123", "")));
    }

    private HashMap<String, byte[]> readDefinitions(String path) throws Exception {
        HashMap<String, byte[]> definitions = new HashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(loadDefinitions(path)));
        ZipEntry ze;
        while ((ze = zip.getNextEntry()) != null) {
            if (!ze.getName().endsWith(".zip") && !ze.getName().endsWith(".jar")) {
                String name = ze.getName();
                InputStream in = zip;
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                int n;
                byte[] buf = new byte[1024];
                while ((n = in.read(buf, 0, 1024)) > -1) {
                    b.write(buf, 0, n);
                }
                definitions.put(name, b.toByteArray());
            }
            zip.closeEntry();
        }
        zip.close();
        return definitions;
    }

    private byte[] loadDefinitions(String src) throws Exception {
        FileInputStream in = new FileInputStream(src);
        byte[] b = new byte[in.available()];
        in.read(b);
        in.close();
        return b;
    }

}
